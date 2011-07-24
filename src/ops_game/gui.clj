(ns ops-game.gui
  (:require [ops-game.opengl :as gl]
            [ops-game.opengl.nifty :as nifty]
            [ops-game.data :as data]
            [ops-game.rendering :as rndr]
            [ops-game.gui.save-and-load :as snl]
            :reload-all))

(def ^{:private true} w 1680)
(def ^{:private true} h 1050)
(def ^{:private true} full false)
(def ^{:private true} fps 36)
(def ^{:private true} map-height 800)

(declare nifty)

(def ^{:private true } pause-draw-flag (atom false))
(defn pause-draw [pause?] (reset! pause-draw-flag pause?))

(defn- draw
  "main drawing function"
  [arg]
  (when-not @pause-draw-flag
    (let [game-data (data/get-drawing-data)
          dims {:left 0 :top (- h map-height) :width w :height map-height}]
      (rndr/draw-game-map game-data dims))))

(defn- handle-unit-movement
  "handles moving the selected unit"
  [loc]
  (when (data/is-unit-current-side?)
    (data/move-selected-unit! loc)))

(defn- update-status-panel
  "updates the various status panels"
  []
  (let [{:keys [selected-unit turn highlighted-hex-type highlighted-hex-cost]} (data/get-status-data)]
    (nifty/update-label-text nifty "game-turn" (str turn))
    (if highlighted-hex-type
      (doto nifty
        (nifty/update-label-text "terrain-type" (str highlighted-hex-type))
        (nifty/update-label-text "terrain-cost" (str highlighted-hex-cost)))
      (doto nifty
        (nifty/update-label-text "terrain-type" "")
        (nifty/update-label-text "terrain-cost" "")))
    (if selected-unit
      (let [{:keys [full-name movement strength type]} selected-unit]
        (doto nifty
          (nifty/update-label-text "unit-name" full-name)
          (nifty/update-label-text "unit-movement" (str movement))
          (nifty/update-label-text "unit-strength" (str strength))
          (nifty/update-label-text "unit-type" (str type))))
      (doto nifty
        (nifty/update-label-text "unit-name" "")
        (nifty/update-label-text "unit-movement" "")
        (nifty/update-label-text "unit-strength" "")
        (nifty/update-label-text "unit-type" "")))))

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

(defn- save-game
  "saves the game" []
  (pause-draw true)
  (snl/show-save-dialog nifty
                        (fn [filename]
                          (when filename (println filename))
                          (pause-draw false))))

(defn- load-game
  "loads the game" []
  (pause-draw true)
  (snl/show-load-dialog nifty
                        (fn [filename]
                          (when filename (data/load-game filename))
                          (pause-draw false))))

(defn- subscribe-event-listeners
  "subscribes all the gui event listeners"
  []
  (nifty/subscribe-event nifty "next-turn-button"
                         (fn [_ _]
                           (data/next-turn!)
                           (update-status-panel))
                         :button-clicked)
  (nifty/subscribe-event nifty "exit-button" (fn [_ _] (gl/stop-main-loop)) :button-clicked)
  (nifty/subscribe-event nifty "save-game-button" (fn [_ _] (save-game)) :button-clicked)
  (nifty/subscribe-event nifty "load-game-button" (fn [_ _] (load-game)) :button-clicked))

(defn initialise-gui
  "Initialises the GUI"
  []
  (try 
    (gl/setup w h full)
    (def nifty (nifty/create))
    (subscribe-event-listeners)
    (snl/initialise-dialogs nifty)
    (rndr/setup-rendering)
    (gl/start-main-loop :nifty nifty :input-handler-fn #(input-dummy %1 %2 %3) :draw-fn #(draw %) :fps fps)
    (catch Throwable e (.printStackTrace e))
    (finally
     (gl/teardown)
     (nifty/destroy nifty))))
