(ns ops-game.swing-ui
  (:use [ops-game.processing]
        [ops-game.data :only [update-hex-under-cursor update-hex-clicked]]
        [ops-game.drawing :only [draw setup coord-to-hex redraw]]
        [seesaw core])
  (:import [java.awt Frame BorderLayout]))

(native!)

(def ^{:private true
       :doc "this is used for development purposes to facilitate restarting the frame"}
  main-frame (atom nil))

(def ^{:private true
       :doc "the action for exiting, currently disposes the main frame"}
  exit-action (action :name "Exit" :key "menu X"
                      :handler (fn [_] (.dispose @main-frame))))

(defn- build-main-menu
  "builds the main menu"
  []
  (menubar
   :items
   [(menu :text "File"
          :items [exit-action]
          )]))

(defn- init-status-panel
  "creates the panel which is used to display status text"
  []  (flow-panel :preferred-size [0 :by 100]))

(defn mouse-moved [applet evt]
  "callback function for when the mouse is moved"
  (let [{x :x y :y} (bean evt)]
    (update-hex-under-cursor (coord-to-hex x y)))
  (redraw applet))

(defn mouse-clicked [applet evt]
  "callback function for when the mouse is clicked"
  (let [{x :x y :y} (bean evt)]
    (update-hex-clicked (coord-to-hex x y)))
  (redraw applet))

(defn- init-main-frame
  "initialises and displays the main window, maximised.  Also handles storing in the atom,
in order to facilitate easy development without restarting"
  []
  (let [applet (make-applet
                {:setup setup
                 :draw #(draw %)
                 :mouseMoved #(mouse-moved %1 %2)
                 :mouseClicked #(mouse-clicked %1 %2)})
        frm (frame
             :title "CMBN Operational Layer"
             :on-close :dispose
             :pack? false
             :menubar (build-main-menu)
             :content (border-panel
                       ;:center applet
                       :south (init-status-panel)))]
    (.setExtendedState frm (bit-or (.getExtendedState frm) Frame/MAXIMIZED_BOTH))
    (.add frm applet BorderLayout/CENTER)
    (if @main-frame (.dispose @main-frame))
    (reset! main-frame frm)))
