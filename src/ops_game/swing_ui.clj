(ns ops-game.swing-ui
  (:use [ops-game.processing]
        [ops-game.data :only [update-hex-under-cursor! update-hex-clicked! update-unit-selected!
                              move-selected-unit! get-status-data is-unit-current-side? next-turn!]]
        [ops-game.drawing :only [draw setup coord-to-hex redraw]]
        [seesaw core])
  (:import [java.awt Frame BorderLayout]
           [java.awt.event MouseEvent]))

(native!)

(def ^{:private true
       :doc "this is used for development purposes to facilitate restarting the frame"}
  main-frame (atom nil))

(def ^{:private true
       :doc "the action for changing the turn"}
  next-turn-action (action :name "Next Turn"
                           :handler (fn [_] (next-turn!)
                                      (.setTitle @main-frame (str (:turn (get-status-data)))))))

(def ^{:private true
       :doc "the action for exiting, currently disposes the main frame"}
  exit-action (action :name "Exit" :key "menu X"
                      :handler (fn [_] (.dispose @main-frame))))

(defn- build-main-menu
  "builds the main menu"
  [] (menubar :items [(menu :text "File" :items [next-turn-action exit-action])]))

(defn- status-label "creates a label with our font"
  [text] (label :font "INCONSOLATA-12" :text text))

(def ^{:private true :doc "the labels that need to change for the various status displays"}
  status-labels
  (let [def-unit-text "no unit selected"
        def-terrain-text "no terrain under cursor"]
    {:unit-name (status-label def-unit-text)
     :unit-strength (status-label def-unit-text)
     :unit-movement (status-label def-unit-text)
     :unit-type (status-label def-unit-text)
     :terrain-type (status-label def-terrain-text)
     :terrain-cost (status-label def-terrain-text)}))

(defn- unit-status-panel
  "creates the unit status panel"
  [] (grid-panel :columns 2
                 :items [(status-label "Selected Unit Information") ""
                         (status-label "Name:") (:unit-name status-labels)
                         (status-label "Strength:") (:unit-strength status-labels)
                         (status-label "Movement:") (:unit-movement status-labels)
                         (status-label "Type:") (:unit-type status-labels)]))

(defn- terrain-status-panel
  "creates the terrain status panel"
  [] (grid-panel :columns 2
                 :items [(status-label "Highlighted Terrain Information") ""
                         (status-label "Type:") (:terrain-type status-labels)
                         (status-label "Movememnt cost:") (:terrain-cost status-labels)]))

(defn- status-panel
  "creates the panel which is used to display status text"
  []  (flow-panel :preferred-size [0 :by 100]
                  :items [(unit-status-panel) (terrain-status-panel)]))

(defn- update-unit-status-panel
  "updates the unit status panel"
  [status]
  (let [{:keys [unit-name unit-strength unit-movement unit-type]} status-labels]
    (if-let [unit (:selected-unit status)]
      (let [{type :type name :full-name movement :movement strength :strength} unit]
        (text! unit-name name)
        (text! unit-type (str type))
        (text! unit-movement (str movement))
        (text! unit-strength (str strength)))
      (doseq [t [unit-name unit-type unit-movement unit-strength]] (text! t "No unit selected")))))

(defn- update-terrain-status-panel
  "updates the terrain status panel"
  [status]
  (let [{:keys [highlighted-hex-type highlighted-hex-cost]} status]
    (text! (:terrain-type status-labels) (when highlighted-hex-type (name highlighted-hex-type)))
    (text! (:terrain-cost status-labels) (str highlighted-hex-cost))))

(defn- update-status-panel
  "updates the status panel"
  [] (invoke-later
      (let [status (get-status-data)]
        (update-unit-status-panel status)
        (update-terrain-status-panel status))))

(defn mouse-moved
  "callback function for when the mouse is moved"
  [applet evt]
  (let [{x :x y :y} (bean evt)]
    (update-hex-under-cursor! (coord-to-hex x y)))
  (redraw applet)
  (update-status-panel))

(defn handle-unit-movement [loc]
  (if (is-unit-current-side?)
    (move-selected-unit! loc)))

(defn mouse-clicked
  "callback function for when the mouse is clicked"
  [applet evt]
  (let [{:keys [x y button]} (bean evt)
        loc (coord-to-hex x y)]
    (if (= button MouseEvent/BUTTON1)
      (do (update-hex-clicked! loc)
          (update-unit-selected! loc))
      (handle-unit-movement loc)))
  (redraw applet)
  (update-status-panel))

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
             :menubar (build-main-menu)
             :content (border-panel
                       :south (status-panel)))]
    (.setExtendedState frm (bit-or (.getExtendedState frm) Frame/MAXIMIZED_BOTH))
    (.add frm applet BorderLayout/CENTER)
    (if @main-frame (.dispose @main-frame))
    (reset! main-frame frm)
    (show! frm)))
