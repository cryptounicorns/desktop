(ns app.main
  (:require [app.config :refer [config]]))

(js/console.log config)

(def electron (js/require "electron"))
(def path (js/require "path"))

(def app (.-app electron))
(def autoUpdater (.-autoUpdater electron))
(def BrowserWindow (.-BrowserWindow electron))
(def Tray (.-Tray electron))
(def ipc (.-ipcMain electron))

(def shell (.-shell electron))

(defn open-in-browser [_ link] (.openExternal shell link))

(goog-define dev? false)

(defn enable-auto-update []
  (.setFeedURL autoUpdater
    (str (:update-endpoint config) "/update/" (.-platfrom js/process) "/" (.getVersion app))))

(def window (atom nil))
(def tray (atom nil))

(defn load-page
  "When compiling with `:none` the compiled JS that calls .loadURL is
  in a different place than it would be when compiling with optimizations
  that produce a single artifact (`:whitespace, :simple, :advanced`).

  Because of this we need to dispatch the loading based on the used
  optimizations, for this we defined `dev?` above that we can override
  at compile time using the `:clojure-defines` compiler option."
  [window]
  (if dev?
    (.loadURL window (str "file://" js/__dirname "/../../index.html"))
    (.loadURL window (str "file://" js/__dirname "/index.html"))))

(defn get-window-position
  []
  (let [window-bounds (.getBounds @window)
        tray-bounds (.getBounds @tray)
        x (.round js/Math
                  (- (.-x window-bounds) (+ (/ (get tray-bounds "x") 2))))
        y (.round js/Math
                  (apply + [10 (.-y tray-bounds) (.-height tray-bounds)]))]
    [x y]))

(defn show-window
  []
  (let [[x y] (get-window-position)]
    (.setPosition @window x y false)
    (.show @window)))

(defn toggle-window [] (if (.isVisible @window) (.hide @window) (show-window)))

(defn make-window
  []
  (BrowserWindow. #js
                   {:x 900
                    :y 30
                    :width 320
                    :height 800
                    :show true
                    :titleBarStyle "hidden"
                    :fullscreenable false
                    :resizable dev?
                    :skipTaskbar true}))

; (defn set-tray!
;   []
;   (let [p (.join path js/__dirname "../../../resources/assets/c@2x.png")]
;     (reset! tray (Tray. p))))

(defn set-tray-event-handlers
  []
  (do (.on @tray "double-click" toggle-window)
      (.on @tray "click" toggle-window)))

(defn init-browser
  []
  (reset! window (make-window))
  (do (load-page @window)
      ;(when dev? (.openDevTools @window #js {:mode "undocked"}))
      (.openDevTools @window #js {:mode "undocked"})
      (.on @window "closed" #(reset! window nil))))

(defn set-title! [_ text] (.setTitle @tray text))

(defn init
  []
  ; (if (= js/process.platform "darwin") (.hide (.-dock app)))
  (.on app "ready" init-browser)
  (do ; (.on app "ready" set-tray!)
      ; (.on app "ready" #(set-title! nil "0000"))
      (when-not dev? (enable-auto-update))
      (when @tray (.on app "ready" set-tray-event-handlers))
      (.on app "browser-window-created" (fn [e w] (.setMenu w (clj->js nil)))))
  (.on ipc "show-window" show-window)
  (.on ipc "set-title" set-title!)
  (.on ipc "open-external" open-in-browser)
  (set! *main-cli-fn* (fn [] nil)))
