(ns app.actions.ui
 (:require [app.db :refer [db router]]))

(defn to-screen [screen]
  (swap! router assoc-in [:screen] screen))

(defn add-to-favs [tupl]
  (swap! db update-in [:favorites] conj tupl))

(defn open-detailed-view [market pair]
  (swap! db assoc-in [:ui/detailed-view] [market pair]))

(defn close-detailed-view []
  (swap! db assoc-in [:ui/detailed-view] nil))

(defn toggle-filter
  "k - keyword of applied filter"
  [k]
  (do
   (swap! db assoc-in [:ui/detailed-view] nil)
   (swap! db update-in [:ui/current-filter]
      #(if (= k (:ui/current-filter @db))
           nil
           k))))
