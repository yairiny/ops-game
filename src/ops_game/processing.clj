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
  (reset! main-applet applet))

(defmacro create-graphics-panel
  "forced to use a macro to keep passing the arguments from outside"
  [& opts#]
  `(let [applet# (defapplet game-applet# ~@opts#)]
    (init-and-save-applet @applet#)))

(defn destroy-graphics-panel
  "destroys the processing applet"
  []
  (.destroy @main-applet))

