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
           [de.lessvoid.nifty.builder ScreenBuilder LayerBuilder PanelBuilder]))

(defn- input-system "creates a merged input system implementation"
  []
  (reify InputSystem
            (forwardEvents [this event-consumer]
              )
            (setMousePosition [this x y]
              (println "setMousePos"))))

(defn- create-screen "creates the nifty screen - this should probably be moved out"
  [nifty]
  (let [screen-builder
            (doto (ScreenBuilder. "main")
              (.controller (DefaultScreenController.))
              (.layer
               (doto (LayerBuilder. "layer")
                 (.backgroundColor "#000f")
                 (.childLayoutVertical)
                 (.panel
                  (let [panel-bldr (PanelBuilder.)]
                    (doto panel-bldr
                      (.id "panel1")
                      (.backgroundColor "#000f")
                      (.height "*")
                      (.width "100%")
                      (.childLayoutCenter))))
                 (.panel
                  (let [panel-bldr (PanelBuilder.)]
                    (doto panel-bldr
                      (.id "panel2")
                      (.backgroundColor "#800f")
                      (.height "250px")
                      (.width "100%")
                      (.childLayoutCenter)))))))]
    (.build screen-builder nifty)))

(defn create "creates the nifty controller and gives it the initial screen"
  []
  (let [nifty (Nifty. (LwjglRenderDevice.) (NullSoundDevice.) (input-system) (TimeProvider.))
        screen (create-screen nifty)]
    (doto nifty
      (.addScreen "mainScreen" screen)
      (.gotoScreen "mainScreen"))))

(defn destroy "destroys the nifty controller"
  [nifty]
  (doto nifty
    (.removeScreen "mainScreen")
    (.exit))
)
