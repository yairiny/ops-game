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

(defn- get-hex-centre
  "gets the pixel coordinate centre of a hex by row and column"
  [row col]
  (let [x (* (int (/ col 2)) 3 *side*) y (* row 2 *ydiff*)]
    (let [x* (+ x *xmarg*) y* (+ y *ymarg*)]
      (if (even? col) [x* y*] [(+ x* *side* *xdiff*) (+ y* *ydiff*)]))))

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

(defn- coord-to-hex-impl
  "returns a [row col] for the given coordinates"
  [x y]
  (let [p (vertices-polygon hex-vertices)
        map-data (:map (data/get-drawing-data))
        positions (for [r (range (count map-data)) c (range (count (first map-data)))] [r c])]
    (first (filter
      (fn [[row col]]
        (let [[px py] (get-hex-centre row col)
              [x* y*] [(- x px) (- y py)]]
          (.contains p (double x*) (double y*)))) positions))))

(def coord-to-hex (memoize coord-to-hex-impl))

(defn setup [applet]
  (.size applet 400 400)
  (.smooth applet)
  (.noLoop applet))

(defn- draw-hex
  "draws a hex on the map"
  [applet row col fill-colour highlight?]
  (apply fill applet (if highlight? (drop-last fill-colour) fill-colour))
  (.pushMatrix applet)
  (apply translate applet (get-hex-centre row col))
  (.beginShape applet)
  (doseq [v hex-vertices]
    (apply vertex applet v))
  (.endShape applet PApplet/CLOSE)
  (.popMatrix applet)
  (.noFill applet))

(defn- draw-map
  "draws the game map layer"
  [applet map-data highlight]
  (doseq [[r row] (map #(vector %1 %2) (range) map-data)]
    (doseq [[c hex-type] (map #(vector %1 %2) (range) row)]
      (draw-hex applet r c (:colour (hex-type terrain-info)) (= highlight [r c])))))

(defn draw "draws the game panel" [applet]
  (.pushMatrix applet)
  (.background applet 255)
  (.noFill applet)
  (let [game-data (data/get-drawing-data)]
    (draw-map applet (:map game-data) (:highlight game-data)))
  (.fill applet 0)
  (.popMatrix applet))

(defn redraw "redraws the game panel" [applet]
  (.redraw applet))
