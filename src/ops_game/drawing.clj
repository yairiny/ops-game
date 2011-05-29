(ns ops-game.drawing
  (:require [ops-game.data :as data])
  (:use [ops-game.drawing data])
  (:use [ops-game.processing])
  (:import [java.awt Polygon]
           [processing.core PApplet]))

(def ^{:private true
       :doc "the length of a hex side"}
  *side* 40)

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

(defonce ^{:private true :doc "memoization of get-hex-centre"}
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

(defonce ^{:doc "memoization of coordinates to their hexes"}
  coord-to-hex (memoize coord-to-hex-impl))

(defn setup "the processing setup function for the graphics panel"
  [applet]
  (.size applet 2000 2000)
  (.background applet 255)
  (.smooth applet)
  (.noLoop applet)
  (.loadShape applet "infantry.svg")
  (let [font (.loadFont applet "Consolas.vlw")]
    (.textFont applet font (int (/ *side* 5)))))

(defn- translate-to-loc
  "causes a translation to a map location"
  [applet row col]
  (apply translate applet (get-hex-centre row col)))

(defn- draw-hex
  "draws a hex on the map"
  [applet row col fill-colour highlight? clicked?]
  (with-pushed-matrix-and-style applet
    (apply fill applet (if highlight? (drop-last fill-colour) fill-colour))
    (if clicked? (.strokeWeight applet 3))
    (translate-to-loc applet row col)
    (make-shape-with-vertices applet PApplet/CLOSE hex-vertices)))

(defn- draw-map
  "draws the game map layer"
  [applet map-data highlight clicked]
  (doseq [[r row] (map #(vector %1 %2) (range) map-data)]
    (doseq [[c hex-type] (map #(vector %1 %2) (range) row)]
      (draw-hex applet r c (:colour (hex-type terrain-info))
                (= highlight [r c]) (= clicked [r c])))))

(defn- draw-unit
  "draws a unit on the map"
  [applet {:keys [location type name]} selected?]
  (with-pushed-matrix-and-style applet
    (apply translate-to-loc applet location)
    (let [base-colour (:colour (unit-info type))
          colour (if selected? (conj base-colour 128) base-colour)]
      (apply fill applet colour))
    (let [l (/ *ydiff* 1.5)]
      (.rect applet (- l) (- l) (* 2 l) (* 2 l))
      (fill applet 0)
      (.text applet name (float (- l)) (float (- l 2))))))

(defn- draw-units
  "draws the units on the map"
  [applet units selected]
  (doseq [unit units]
    (draw-unit applet unit (= unit selected))))

(defn draw "draws the game panel" [applet]
  (try
    (.background applet 255)
    (with-pushed-matrix-and-style applet
      (let [game-data (data/get-drawing-data)]
        (draw-map applet (:map game-data) (:highlight game-data) (:clicked game-data))
        (draw-units applet (:units game-data) (:selected-unit game-data))))
    (catch Exception e (.printStackTrace e))))

(defn redraw "redraws the game panel" [applet]
  (.redraw applet))
