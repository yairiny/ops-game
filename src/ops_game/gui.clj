(ns ops-game.gui
  (:require [ops-game.opengl :as gl]
            [ops-game.data :as data]
            [ops-game.rendering :as rndr]
            :reload-all))

(def ^{:private true} w 1680)
(def ^{:private true} h 1050)
(def ^{:private true} full false)
(def ^{:private true} fps 30)
(def ^{:private true} map-height 800)


(defn- draw
  "main drawing function"
  [arg]
  (let [game-data (data/get-drawing-data)
        dims {:left 0 :top (- h map-height) :width w :height map-height}]
    (rndr/draw-game-map game-data dims)))

(defn- input-dummy [k m a]
  (when m
    (let [[x y] (gl/get-mouse-pos)
          y (- h y)
          hex (rndr/screen-to-hex [x y])]
      (when hex
        (data/update-hex-under-cursor! hex))))
  a)

(defn initialise-gui
  "Initialises the GUI"
  []
  (try 
    (gl/setup w h full)
    (rndr/setup-rendering)
    (gl/start-main-loop :input-handler-fn #(input-dummy %1 %2 %3) :draw-fn #(draw %) :fps fps)
    (finally
     (gl/teardown))))
