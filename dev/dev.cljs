(ns dev)
;   (:require [clojure.pprint :refer [pprint]]
;             [mount.core :as mount]
;             [app.renderer]))

; (enable-console-print!)

; (defn start [] (mount/start))

; (defn go [] (mount/stop))

; (defn reset [] (go))

(ns dev.core
  (:require [figwheel.client :as fw :include-macros true]
            [app.renderer]))

(fw/watch-and-reload :websocket-url "ws://localhost:3449/figwheel-ws"
                     :jsload-callback (fn [] (print "reloaded")))
