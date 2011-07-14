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
           [de.lessvoid.nifty.controls Label]))

(defn- input-system "creates a merged input system implementation"
  []
  (reify InputSystem
            (forwardEvents [this event-consumer])
            (setMousePosition [this x y])))

(defn create "creates the nifty controller and gives it the initial screen"
  []
  (let [nifty (Nifty. (LwjglRenderDevice.) (NullSoundDevice.) (input-system) (TimeProvider.))]
    (.setLevel (java.util.logging.Logger/getLogger "de.lessvoid.nifty") java.util.logging.Level/SEVERE)
    (doto nifty
      (.loadStyleFile "nifty-default-styles.xml")
      (.loadControlFile "nifty-default-controls.xml")
      (.fromXmlWithoutStartScreen "data/gui.xml")
      (.gotoScreen "main"))))

(defn destroy "destroys the nifty controller"
  [nifty]
  (doto nifty
    (.removeScreen "mainScreen")
    (.exit)))

(defn update-label-text "updates the text for a label"
  [nifty id text]
  (.. nifty (getCurrentScreen) (findNiftyControl id Label) (setText text)))
