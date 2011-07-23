(ns ops-game.opengl.nifty
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.input Keyboard Mouse]
           [de.lessvoid.nifty Nifty]
           [de.lessvoid.nifty.sound SoundSystem]
           [de.lessvoid.nifty.tools TimeProvider]
           [de.lessvoid.nifty.renderer.lwjgl.render LwjglRenderDevice]
           [de.lessvoid.nifty.spi.input InputSystem]
           [de.lessvoid.nifty.nulldevice NullSoundDevice]
           [de.lessvoid.nifty.sound.openal OpenALSoundDevice]
           [de.lessvoid.nifty.screen Screen DefaultScreenController]
           [de.lessvoid.nifty.builder ScreenBuilder LayerBuilder PanelBuilder]
           [de.lessvoid.nifty.controls.label.builder LabelBuilder]
           [de.lessvoid.nifty.controls Label ListBox]))

(defn destroy "destroys the nifty controller"
  [nifty]
  (doto nifty
    (.removeScreen "mainScreen")
    (.exit)))

(defn update-label-text "updates the text for a label"
  [nifty id text]
  (.. nifty (getCurrentScreen) (findNiftyControl id Label) (setText text)))

(def ^{:private true :doc "nifty event queue"} event-queue
  (ref (clojure.lang.PersistentQueue/EMPTY)))

(defn report-event
  "reports an event to nifty for processing by the reified input system"
  [event]
  (dosync
   (alter event-queue conj event)))

(defn- get-next-event
  "pops the next event from the queue in a synchronised manner"
  []
  (dosync
   (let [ret (first @event-queue)]
     (alter event-queue pop)
     ret)))

(defn- input-system "creates a merged input system implementation"
  []
  (reify InputSystem
    (forwardEvents [this event-consumer]
      (doseq [event (take-while identity (repeatedly get-next-event))]
        (when (:mouse event)
          (let [{:keys [x y wheel button down]} event]
            (.processMouseEvent event-consumer x y wheel button down)))))
    (setMousePosition [this x y])))

(defn create "creates the nifty controller and gives it the initial screen"
  []
  (.setLevel (java.util.logging.Logger/getLogger "de.lessvoid") java.util.logging.Level/WARNING)
  (.setLevel (java.util.logging.Logger/getLogger "de.lessvoid.nifty.Nifty") java.util.logging.Level/WARNING)
  (let [nifty (Nifty. (LwjglRenderDevice.) (NullSoundDevice.) (input-system) (TimeProvider.))]
    (doto nifty
      (.loadStyleFile "nifty-default-styles.xml")
      (.loadControlFile "nifty-default-controls.xml")
      (.fromXml "data/gui.xml" "main"))))

(defn subscribe-event 
  "subscribes to an event"
  [nifty id handler-fn] 
  {:pre [nifty id (fn? handler-fn)]}
  (let [screen (.getCurrentScreen nifty)]
    (.subscribe nifty screen id de.lessvoid.nifty.controls.ButtonClickedEvent
                (reify org.bushe.swing.event.EventTopicSubscriber
                  (onEvent [this topic event]
                    (handler-fn topic event))))))

(defn show-popup 
  "shows a popup layer"
  [nifty popup-id] 
  {:pre [nifty (string? popup-id)]}
  (let [popup-element (.createPopup nifty popup-id)
        element-id (.getId popup-element)]
    (.showPopup nifty (.getCurrentScreen nifty) element-id  nil)
    element-id))

(defn hide-popup
  "hides a popup layer"
  [nifty layer-id]
  {:pre [nifty (string? layer-id)]}
  (.closePopup nifty layer-id))

(defn add-items-to-list
  "adds the items to a list box with the given id"
  [nifty popup-id id items]
  {:pre [nifty (string? popup-id)(string? id) (vector? items)]}
  (.. nifty (findPopupByName popup-id) (findNiftyControl id ListBox) (addAllItems items)))

(defn get-selected-items
  "gets the selected items from a list box with the given id"
  [nifty popup-id id]
  {:pre [nifty (string? popup-id) (string? id)]}
  (vec (.. nifty (findPopupByName popup-id) (findNiftyControl id ListBox) (getSelection))))
