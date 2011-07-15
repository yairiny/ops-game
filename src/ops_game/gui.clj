(ns ops-game.gui
  (:require [ops-game.opengl :as gl]
            [ops-game.opengl.nifty :as nifty]
            [ops-game.data :as data]
            [ops-game.rendering :as rndr]
            :reload-all))

(def ^{:private true} w 1680)
(def ^{:private true} h 1050)
(def ^{:private true} full false)
(def ^{:private true} fps 12)
(def ^{:private true} map-height 800)

(declare nifty)

(defn- draw
  "main drawing function"
  [arg]
  (let [game-data (data/get-drawing-data)
        dims {:left 0 :top (- h map-height) :width w :height map-height}]
    (rndr/draw-game-map game-data dims)))

(defn- handle-unit-movement
  "handles moving the selected unit"
  [loc]
  (when (data/is-unit-current-side?)
    (data/move-selected-unit! loc)))

(defn- update-status-panel
  "updates the various status panels"
  []
  (let [{:keys [selected-unit]} (data/get-status-data)]
    (if selected-unit
      (let [{:keys [full-name movement]} selected-unit]
        (doto nifty
          (nifty/update-label-text "unit-name" full-name)
          (nifty/update-label-text "unit-movement" (str movement))))
      (doto nifty
        (nifty/update-label-text "unit-name" "")
        (nifty/update-label-text "unit-movement" "")))))

(defn- input-dummy [k m a]
  (when m
    (let [[x y] (gl/get-mouse-pos)
          y (- h y)
          hex (rndr/screen-to-hex [x y])]
      (when hex
        (data/update-hex-under-cursor! hex)
        (when (:left (gl/get-mouse-buttons))
          (data/update-unit-selected! hex))
        (when (:right (gl/get-mouse-buttons))
          (handle-unit-movement hex))
        (update-status-panel))))
  a)

(defn- subscribe-event-listeners
  "subscribes all the gui event listeners"
  []
  (nifty/subscribe-event nifty "exit-button" (fn [_ _] (gl/stop-main-loop))))

(defn initialise-gui
  "Initialises the GUI"
  []
  (try 
    (gl/setup w h full)
    (def nifty (nifty/create))
    (subscribe-event-listeners)
    (rndr/setup-rendering)
    (gl/start-main-loop :nifty nifty :input-handler-fn #(input-dummy %1 %2 %3) :draw-fn #(draw %) :fps fps)
    (catch Throwable e (.printStackTrace e))
    (finally
     (gl/teardown)
     (nifty/destroy nifty))))
