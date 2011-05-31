(ns ops-game.test.processing
  (:use ops-game.processing)
  (:import [javax.swing JFrame JButton]
           [java.awt BorderLayout]))

(defn- test-setup [this]
  (.size this 400 400)
  (.noLoop this))

(defonce ^{:private true} last-place (atom 0))

(defn- test-draw [this]
  (.background this 255)
  (.line this 10 10 (+ 20 (swap! last-place inc)) 300))

(defn- test-mouse-moved [this evt]
  (.redraw this))

(defn- test-main []
  (let [frm (JFrame. "Processing Tester")
        applet (make-applet {:setup test-setup :draw #(test-draw %) :mouseMoved test-mouse-moved})]
    (.setLayout frm (BorderLayout.))
    (.add frm applet BorderLayout/CENTER)
    (.setVisible frm true)
    [frm applet]))
