(ns ops-game.processing
  (:use [rosado.processing]
        [rosado.processing.applet]))

(def ^{:private true
       :doc "holds the applet object"}
  main-applet (atom nil))

(defn create-graphics-panel
  "creates the processing applet, i.e. the panel in which we display our graphics"
  []
  (let [applet (defapplet game-applet :draw #(line 10 10  (frame-count) 100))]
    (.init @applet)
    (reset! main-applet @applet)))

(defn destroy-graphics-panel
  "destroys the processing applet"
  []
  (.destroy @main-applet))
