(ns app.screens.portfolio
  (:require [clojure.string :as s]
            [reagent.core :as r]
            [goog.object :as gobj]
            [cljsjs.react-select]
            [cljsjs.moment]
            [app.components.header :refer [header]]
            [app.actions.ui :refer [to-screen]]
            [app.db :refer [db]]
            [app.components.ui :as ui]
            [app.motion :refer [Motion spring presets]]
            [app.logic.curr :refer [get-market-names get-crypto-currs]]
            [app.logic.validation :refer
             [str->amount validate-portfolio-record]]
            [app.actions.ui :refer
             [toggle-edit-portfolio-view
              open-add-portfolio-view
              close-add-portfolio-view
              close-every-portfolio-view]]
            [app.actions.form :refer
             [update-portfolio-form
              clear-portfolio-form]]
            [app.actions.portfolio :refer
             [create-portfolio-record
              remove-portfolio-record
              update-portfolio-record
              get-total-worth]]))

;; event handlers are composed of granular api calls

(defn handle-delete
  []
  (remove-portfolio-record (-> @db
                               :ui/folio-edit))
  (close-every-portfolio-view))

(defn handle-close []
  (close-every-portfolio-view)
  (clear-portfolio-form))

(defn handle-change
  [e]
  (let [v (-> e
              .-target
              .-value)]
    (update-portfolio-form :amount (str->amount v))))

(defn handle-submit
  []
  (when-let [a (validate-portfolio-record (-> @db
                                              :form/portfolio))]
    (do (create-portfolio-record a)
        (close-every-portfolio-view)
        (clear-portfolio-form))))

;; components below

(defn handle-edit
  []
  (when-let [a (validate-portfolio-record (-> @db
                                              :form/portfolio))]
    (do (update-portfolio-record a)
        (close-every-portfolio-view)
        (clear-portfolio-form))))

(defn- total-worth
  []
  (fn []
    (let [w (.toFixed (get-total-worth) 2)]
      (if (pos? w) [:div.total_worth (str "$ " w)] [:div]))))

(defn portfolio-list
  []
  (let [folio @(r/track #(-> @db
                             :user
                             :portfolio))]
    [:div.portfolio_items_wrapper
     (if-not (pos? (count (vals folio)))
       [ui/empty-list "portfolio items"]
       (for [row (vals folio)]
         (let [{:keys [currency amount market id added]} row]
           ^{:key id}
           [:div.row_wrap
            ^{:key "currency"}
            {:on-click #(toggle-edit-portfolio-view id)}
            [:div.left_cell
             [:div.title (str (name currency) " " amount)]
             [:div.market market]]
            ^{:key "last-ctrls"}
            [:div.right_cell
             [:div.actions
              [:div.ts
                (.format
                  (js/moment added)
                  "hh:mm:ss\n MM/DD/YYYY")]]]])))]))

(defn select-market
  []
  (let [m (-> @db
              :markets)
        v (-> @db
              :form/portfolio
              :market)
        opts (get-market-names m)
        on-change #(update-portfolio-form
                    :market
                    (if % (aget % "value") (update-portfolio-form :market "")))]
    [:>
     js/window.Select
     {:value v
      :options (clj->js (map #(zipmap [:value :label] [% %]) opts))
      :onChange on-change}]))

(defn select-curr
  []
  ;; TODO: only allow currency available on selected market
  (let [m (-> @db
              :markets)
        v (-> @db
              :form/portfolio
              :currency)
        opts (get-crypto-currs m)
        on-change
        #(update-portfolio-form
          :currency
          (if % (aget % "value") (update-portfolio-form :currency "")))]
    [:>
     js/window.Select
     {:value v
      :options (clj->js (map #(zipmap [:value :label] [% %]) opts))
      :onChange on-change}]))

(defn edit-item
  []
  [:div.form_wrap
   [:h1 "Edit holding"]
   [ui/close "left_top" #(handle-close)]
   [ui/input-wrap "Market" [select-market {:key "market"}]]
   [ui/input-wrap "Currency" [select-curr {:key "currency"}]]
   [ui/text-input
    {:on-change handle-change
     :value #(-> @db
                 :form/portfolio
                 :amount)
     :label "amount"}]
   [:div.input_wrapper
    [ui/button
     {:on-click handle-delete
      :color "red"}
     "Delete"]
    [ui/button
     {:on-click handle-edit
      :color "#000"}
     "Save"]]])

(defn add-item
  []
  [:div.form_wrap
   [:h1 "Add holding"]
   [ui/close "left_top" #(handle-close)]
   [ui/input-wrap "Market" [select-market {:key "market"}]]
   [ui/input-wrap "Currency" [select-curr {:key "currency"}]]
   [ui/text-input
    {:on-change handle-change
     :value #(-> @db
                 :form/portfolio
                 :amount)
     :label "amount"}]
   [:div.input_wrapper
    [ui/button
     {:on-click handle-submit
      :color "#000"}
     "Add"]]])

(def animated-view-edit
  (r/reactify-component (fn [{c :children}]
                          (let [y (gobj/get c "y")]
                            [:div.detailed_view
                             {:ref #(swap! db update-in
                                     [:ui/folio-edit-height]
                                     (fn [] (if % (.-offsetHeight %) 0)))
                              :style {:transform (str "translateY(" y "px)")}}
                             [edit-item]]))))

(defn detailed-view-edit
  []
  (fn []
    (let [open? @(r/cursor db [:ui/folio-edit])]
      [:div.motion_wrapper
       [Motion
        {:style {:y (spring (if open?
                              (- (:ui/folio-edit-height @db))
                              0))}}
        (fn [y] (r/create-element animated-view-edit #js {} y))]])))

(def animated-view-add
  (r/reactify-component (fn [{c :children}]
                          (let [y (gobj/get c "y")]
                            [:div.detailed_view
                             {:ref #(swap! db update-in
                                     [:ui/folio-add-height]
                                     (fn [] (if % (.-offsetHeight %) 0)))
                              :style {:transform (str "translateY(" y "px)")}}
                             [add-item]]))))

(defn detailed-view-add
  []
  (fn []
    (let [open? @(r/cursor db [:ui/folio-add])]
      [:div.motion_wrapper
       [Motion
        {:style {:y (spring (if open?
                              (- (:ui/folio-add-height @db))
                              0))}}
        (fn [y] (r/create-element animated-view-add #js {} y))]])))

(defn portfolio-toolbar
  [s]
  (let [open? (not (or (:ui/folio-edit @db)
                       (:ui/folio-add @db)))]
    (when open?
      [:div.portfolio_toolbar
       [:div
        {:style {:padding "0 10px"
                 :width "100%"}}
        [ui/button
         {:on-click #(open-add-portfolio-view)
          :color "#000"}
         "Add"]]])))

(defn portfolio
  []
  [:div.portfolio_container
   [header]
   [total-worth]
   [portfolio-list]
   [portfolio-toolbar]
   [detailed-view-edit]
   [detailed-view-add]])
