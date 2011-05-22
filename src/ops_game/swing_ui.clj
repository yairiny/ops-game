(ns ops-game.swing-ui
  (:use [ops-game.processing]
        [seesaw core])
  (:import java.awt.Frame))

(native!)

(def ^{:private true
       :doc "This is used for development purposes to facilitate restarting the frame"}
  main-frame (atom nil))

(def ^{:private true
       :doc "The action for exiting, currently disposes the main frame"}
  exit-action (action :name "Exit" :key "menu X"
                      :handler (fn [_] (do (.dispose @main-frame) (destroy-graphics-panel)))))

(defn- build-main-menu
  "Builds the main menu"
   []
  (menubar
   :items
   [(menu :text "File"
          :items [exit-action])]))

(defn- init-status-panel
  "creates the panel which is used to display status text"
  []  (flow-panel :preferred-size [0 :by 100]))

(defn- init-main-frame
  "Initialises and displays the main window, maximised.  Also handles storing in the atom,
in order to facilitate easy development without restarting"
  []
  (let [frm (frame
             :title "CMBN Operational Layer"
             :on-close :nothing
             :pack? true
             :menubar (build-main-menu)
             :content (border-panel
                       :center (create-graphics-panel)
                       :south (init-status-panel)))]
    (.setExtendedState frm (bit-or (.getExtendedState frm) Frame/MAXIMIZED_BOTH))
    (if @main-frame (.dispose @main-frame))
    (reset! main-frame frm)))
