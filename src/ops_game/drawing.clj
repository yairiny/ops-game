(ns ops-game.drawing
  (:require [ops-game.data :as data])
  (:use [ops-game.drawing data])
  (:use [ops-game.processing])
  (:import [java.awt Polygon]
           [processing.core PApplet]))

(def ^{:private true
       :doc "the length of a hex side"}
  *side* 30)

(def ^{:private true
       :doc "the horizontal pixel difference factor"}
  *xdiff* (* (PApplet/sin (PApplet/radians 30)) *side*))

(def ^{:private true
      :doc "the vertical pixel difference factor"}
  *ydiff* (* (PApplet/cos (PApplet/radians 30)) *side*))

(def ^{:private true
      :doc "the x margin"}
  *xmarg* (+ 5 *side*))

(def ^{:private true
      :doc "the y margin"}
  *ymarg* (+ 10 *ydiff*))

(defn- get-hex-centre-impl
  "gets the pixel coordinate centre of a hex by row and column"
  [row col]
  (let [x (* (int (/ col 2)) 3 *side*) y (* row 2 *ydiff*)]
    (let [x* (+ x *xmarg*) y* (+ y *ymarg*)]
      (if (even? col) [x* y*] [(+ x* *side* *xdiff*) (+ y* *ydiff*)]))))

(def ^{:private true :doc "memoization of get-hex-centre"}
  get-hex-centre (memoize get-hex-centre-impl))

(def ^{:private true
       :doc "the vertices that compose a hex"}
  hex-vertices [[(- *xdiff*) (- *ydiff*)]
                [*xdiff* (- *ydiff*)]
                [*side* 0]
                [*xdiff* *ydiff*]
                [(- *xdiff*) *ydiff*]
                [(- *side*) 0]])

(defn- vertices-polygon
  "creates a java.awt.Polygon from a sequence of vertices"
  [vertices]
  (let [p (Polygon.)]
    (doseq [[x y] vertices] (.addPoint p x y))
    p))

(defn- gridify
  "reduces granularity of coordinate values"
  [xs]
  (map #(- % (rem % 5)) xs))

(defn- coord-to-hex-impl
  "returns a [row col] for the given coordinates"
  [x y]
  (let [[x y] (gridify [x y])
        p (vertices-polygon hex-vertices)
        map-data (:map (data/get-drawing-data))
        positions (for [r (range (count map-data)) c (range (count (first map-data)))] [r c])]
    (first (filter
      (fn [[row col]]
        (let [[px py] (get-hex-centre row col)
              [x* y*] [(- x px) (- y py)]]
          (.contains p (double x*) (double y*)))) positions))))

(def ^{:doc "memoization of coordinates to their hexes"}
  coord-to-hex (memoize coord-to-hex-impl))

(defn setup "the processing setup function for the graphics panel"
  [applet]
  (.size applet 2000 2000)
  (.background applet 255)
  (.smooth applet)
  (.noLoop applet))

(defn- draw-hex
  "draws a hex on the map"
  [applet row col fill-colour highlight? clicked?]
  (with-pushed-matrix-and-style applet
    (apply fill applet (if highlight? (drop-last fill-colour) fill-colour))
    (if clicked? (.strokeWeight applet 3))
    (apply translate applet (get-hex-centre row col))
    (make-shape-with-vertices applet PApplet/CLOSE hex-vertices)))

(defn- draw-map
  "draws the game map layer"
  [applet map-data highlight clicked]
  (doseq [[r row] (map #(vector %1 %2) (range) map-data)]
    (doseq [[c hex-type] (map #(vector %1 %2) (range) row)]
      (draw-hex applet r c (:colour (hex-type terrain-info))
                (= highlight [r c]) (= clicked [r c])))))

(defn draw "draws the game panel" [applet]
  (.background applet 255)
  (with-pushed-matrix-and-style applet
    (let [game-data (data/get-drawing-data)]
      (draw-map applet (:map game-data) (:highlight game-data) (:clicked game-data)))))

(defn redraw "redraws the game panel" [applet]
  (.redraw applet))
