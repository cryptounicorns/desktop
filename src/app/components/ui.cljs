(ns app.components.ui
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [app.db :refer [db]]))

(defn Spinner
  []
  [:div.orbit-spinner
   (for [i [1 2 3]]
    ^{:key i}
    [:div.orbit])])

(defn Button
  [params text]
  (let [{:keys [on-click type ref disabled color]} params]
    [:button.button
     (merge (when color {:style {:background-color color}})
            {:on-click on-click :ref ref :type type})
     text]))

(defn Icon
  [on-click src]
  [:img
   {:src src
    :on-click on-click
    :style {:width "20px"
            :height "20px"
            ; :&:hover {:cursor "pointer"}
            ; :&:active {:opacity ".5"}
            :-webkit-user-select "none"}}])

(defn Checkbox
  [legend value on-change]
  ;; generage custom "for"
  [:div.checkbox_wrapper
   [:div.checkbox_legend legend]
   [:input#tray.checkbox {:type "checkbox" :on-change on-change}]
   [:label {:for "tray"}]])

(defn EmptyListCompo
  [items]
  [:div.form_empty_list (str "You haven't added any " items " yet")])

(defn InputWrapper
  "Wraps the input and provides label"
  [label & children]
  [:div.input_wrapper [:div.input_label label] children])

(defn CurrInput
  "Generic text/number input"
  [on-change cursor]
  (fn []
    [:div.input_wrapper
     [:div.input_label "Amount"]
     [:input.input_item
      {:type "text"
       :autoFocus false
       :onChange on-change
       :value (-> @db
                  cursor
                  :amount)}]]))

