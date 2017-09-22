(ns app.screens.markets
  (:require [reagent.core :as reagent :refer [atom]]
            [app.db :refer [db]]
            [app.actions :as actions]
            [app.logic :as logic]
            [app.constants.currs :refer [pairs]]
            [app.components.header :refer [header]]))

(defn get-markets []
  (reduce
   (fn [acc [key val]]
    (conj acc {:name key
               :pairs-num (count (keys val))}))
   []
   (:markets @db)))

(defn markets []
  (let [markets (get-markets)]
    [:div.markets_wrapper
     [header]
     (for [m markets]
        (let [{:keys [name pairs-num]} m]
          ^{:key name}
          [:div.market_row
            [:div.item
             [:div.name name]
             [:div.number "BTC Volume"]
             [:div.number "Number of pairs:"]]
            [:div.item
             [:div.name.right.green "●"]
             [:div.number.right "100000"]
             [:div.number.right pairs-num]]]))]))

