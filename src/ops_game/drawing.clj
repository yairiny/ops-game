(ns ops-game.drawing
  (:require [ops-game.data :as data])
  (:use [ops-game.drawing data])
  (:use [rosado.processing])
  (:import [java.awt Polygon]))

(def ^{:private true
       :doc "the length of a hex side"}
  *side* 30)

(def ^{:private true
       :doc "the horizontal pixel difference factor"}
  *xdiff* (* (sin (radians 30)) *side*))

(def ^{:private true
      :doc "the vertical pixel difference factor"}
  *ydiff* (* (cos (radians 30)) *side*))

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

(comment (defn coord-to-hex
   "returns a [row col] for the given coordinates"
   [x y]
   (let [p (vertices-polygon hex-vertices)
         map-data (:map (data/get-drawing-data))
         positions (for [r (range (count map-data)) c (range (count (first map-data)))] [r c])]
     (filter
      (fn [[row col]]
        (let [[px py] (get-hex-centre row col)
              [x* y*] [(- x px) (- y py)]]
          (.contains p (double x*) (double y*)))) positions))))

(defn coord-to-hex [x y] [4 5])

(defn setup []
  (size 2000 2000)
  (smooth)
  (no-loop)
  )

(defn- draw-hex
  "draws a hex on the map"
  [row col fill-colour highlight?]
  (apply fill (if highlight? (drop-last fill-colour) fill-colour))
  (push-matrix)
  (apply translate (get-hex-centre row col))
  (begin-shape)
  (doseq [v hex-vertices]
    (apply vertex v))
  (end-shape CLOSE)
  (pop-matrix)
  (no-fill))

(defn- draw-map
  "draws the game map layer"
  [map-data highlight]
  (println "highlight" highlight)
  (doseq [[r row] (map #(vector %1 %2) (range) map-data)]
    (doseq [[c hex-type] (map #(vector %1 %2) (range) row)]
      (draw-hex r c (:colour (hex-type terrain-info)) (= highlight [r c])))))

(defn draw "draws the game panel" []
  (println "drawing")
  (push-matrix)
  (background 255)
  (no-fill)
  (let [game-data (data/get-drawing-data)]
    (draw-map (:map game-data) (:highlight game-data)))
  (fill 0)
  (pop-matrix))

(defn redraw-panel "redraws the game panel" []
  (redraw))
