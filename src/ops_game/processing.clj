(ns ops-game.processing
  (:use [rosado.processing]
        [rosado.processing.applet]))

(def ^{:private true
       :doc "holds the applet object"}
  main-applet (atom nil))

(defn init-and-save-applet
  "initialises the applet and saves the reference"
  [applet]
  (.init applet)
  
  "destroys the processing applet"
  []
  (.destroy @main-applet))

(defn draw-func []
  (line 10 10 (frame-count) 100))
