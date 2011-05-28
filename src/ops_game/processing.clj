(ns ops-game.processing
  (:import [javax.swing JFrame JButton]
           [java.awt BorderLayout]
           [processing.core PApplet]))

(defn- test-setup [this]
  (.size this 400 400)
  (.noLoop this))

(def ^{:private true} last-place (atom 0))

(defn- test-draw [this]
  (.background this 255)
  (.line this 10 10 (+ 20 (swap! last-place inc)) 300))

(defn- test-mouse-moved [this evt]
  (.redraw this))

(defn make-applet [funcmap]
  (defn- get-func [name]
    (if (funcmap name) (funcmap name) (fn [& _])))
  (let [p
        (proxy [PApplet] []
          (setup [] (apply (get-func :setup) [this]))
          (draw [] (apply (get-func :draw) [this]))
          (mouseMoved [evt] (apply (get-func :mouseMoved) [this evt])))]
    (.init p)
    p))

(defn- test-main []
  (let [frm (JFrame. "Processing Tester")
        applet (make-applet {:setup test-setup :draw #(test-draw %) :mouseMoved test-mouse-moved})]
    (.setLayout frm (BorderLayout.))
    (.add frm applet BorderLayout/CENTER)
    (.setVisible frm true)
    [frm applet]))

(defn fill
  ([applet r g b a] (.fill applet r g b a))
  ([applet rgb] (.fill applet rgb))
  ([applet r g b] (.fill applet r g b)))

(defn translate
  ([applet x y] (.translate applet x y)))

(defn vertex
  ([applet x y] (.vertex applet x y)))
