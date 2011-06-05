(ns ops-game.processing
  (:import [processing.core PApplet]))

(defn make-applet
  "makes a new applet object
   func-map is a map of functions with :draw, :setup, :mouseMoved
   and :mouseClicked as possible keys"
  [funcmap]
  (defn- get-func [name]
    (if (funcmap name) (funcmap name) (fn [& _])))
  (let [p
        (proxy [PApplet] []
          (setup [] ((get-func :setup) this))
          (draw [] ((get-func :draw) this))
          (mouseMoved [evt] ((get-func :mouseMoved) this evt))
          (mouseClicked [evt] ((get-func :mouseClicked) this evt)))]
    (.init p)
    p))

(defn fill
  "wraps the fill function in processing"
  ([applet r g b a] (.fill applet r g b a))
  ([applet rgb] (.fill applet rgb))
  ([applet r g b] (.fill applet r g b)))

(defn translate
  "wraps the translate function in processing"
  ([applet x y] (.translate applet x y)))

(defn vertex
  "wraps the vertex function in processing"
  ([applet x y] (.vertex applet x y)))

(defmacro with-pushed-matrix-and-style
  "performs the body wrapped in pushing matrix and style and then popping them"
  [applet & body]
  `(do (.pushMatrix ~applet)
       (.pushStyle ~applet)
       (try
         ~@body
         (catch Exception e# (.printStackTrace e#)))
      (.popStyle ~applet)
      (.popMatrix ~applet)))

(defn make-shape-with-vertices
  "makes a shape with the given 2d vertices sequence.  kind is one of the endShape parameters"
  [applet kind vertices]
  (.beginShape applet)
  (doseq [v vertices]
    (apply vertex applet v))
  (.endShape applet kind))
