(ns ops-game.opengl
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
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

(defn- set-display-mode [width height fullscreen]
  "sets the display mode according to the provided parameters"
  {:pre (> width 0) (> height 0) (#{true false} fullscreen)}
  (Display/setDisplayMode
   (first (filter (fn [mode]
                    (and (= width (.getWidth mode))
                         (= height (.getHeight mode))
                         (= 32 (.getBitsPerPixel mode))))
                  (Display/getAvailableDisplayModes))))
  (Display/setFullscreen fullscreen))

(defn setup
  "setup the open gl context and main window"
  [width height fullscreen]
  {:pre (> width 0) (> height 0) (#{true false} fullscreen)}
  (let [] 
    (set-display-mode width height fullscreen)
    (Display/create)
    
    (Keyboard/create)
    (Mouse/create)

    (GL11/glEnable GL11/GL_BLEND)
    (GL11/glBlendFunc GL11/GL_SRC_ALPHA, GL11/GL_ONE_MINUS_SRC_ALPHA)

    (GL11/glDisable GL11/GL_DEPTH_TEST)
    (GL11/glEnable GL11/GL_TEXTURE_2D)
    
    (GL11/glMatrixMode GL11/GL_PROJECTION)
    (GL11/glLoadIdentity)
    (GL11/glOrtho 0 width height 0 1 -1)
    (GL11/glMatrixMode GL11/GL_MODELVIEW)
    (GL11/glEnable GL11/GL_RGBA_MODE)
    (GL11/glClearColor 1.0 1.0 1.0 0)

    (let [input-system
          (reify InputSystem
            (forwardEvents [this event-consumer]
              )
            (setMousePosition [this x y]
              (println "setMousePos"))
            )]
      (def nifty (Nifty. (LwjglRenderDevice.) (NullSoundDevice.) input-system (TimeProvider.)))
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
                      (.id "panel")
                      (.backgroundColor "#000f")
                      (.height "*")
                      (.width "100%")
                      (.childLayoutCenter))))
                 (.panel
                  (let [panel-bldr (PanelBuilder.)]
                    (doto panel-bldr
                      (.id "panel")
                      (.backgroundColor "#800f")
                      (.height "250px")
                      (.width "100%")
                      (.childLayoutCenter)))))))
            screen (.build screen-builder nifty)]
        (.addScreen nifty "mainScreen" screen)
        (.gotoScreen nifty "mainScreen"))
      )
    ))

(defn teardown
  "destroys the open gl context and cleans up" 
  []
  (Mouse/destroy)
  (Keyboard/destroy)
  (Display/destroy))

(defn- start-main-loop*
  "implementation of the main loop function"
  [{:keys [input-handler-fn draw-fn fps input-handler-fn-arg draw-fn-arg]
    :or {input-handler-fn-arg nil draw-fn-arg nil} :as args}]
  {:pre [(fn? input-handler-fn) (fn? draw-fn) (integer? fps) (> fps 0)]}
  (when (not (Display/isCloseRequested))
    (GL11/glLoadIdentity)
    (.render nifty false)
    (.update nifty)
    (let [keyboard (Keyboard/next)
          mouse (Mouse/next)
          input-ret (if (or keyboard mouse)
                      (input-handler-fn keyboard mouse input-handler-fn-arg)
                      input-handler-fn-arg)
          draw-ret (draw-fn draw-fn-arg)
          ]
      (Display/update)
      (Display/sync fps)
      (recur (merge args {:input-handler-fn-arg input-ret :draw-fn-arg :draw-ret})))))

(defn start-main-loop [& args]
  "starts the main program loop. parameters are as follows:
:input-handler-fn - (fn [kb-event? mouse-event? input-fn-arg]
:draw-fn - (fn [draw-fn-arg]
:fps - target fps
:input-handler-fn-arg - the initial argument to the input handler
:draw-fn-arg - the initial argument to the draw function
the arguments are optional and the return value of each function will be passed as the argument to the next call"
  (start-main-loop* (apply hash-map (vec args))))

(defn start-scissor-test [x y w h]
  "starts the scissor test using the given dimension"
  {:pre (every? #(>= % 0) [x y w h])}
  (GL11/glEnable GL11/GL_SCISSOR_TEST)
  (GL11/glScissor x y w h))

(defn stop-scissor-test []
  "stops the scissor test"
  (GL11/glDisable GL11/GL_SCISSOR_TEST))

(defmacro with-scissor-test
  "performs body with the given scissor test"
  [x y w h & body]
  `(try
     (start-scissor-test ~x ~y ~w ~h)
     ~@body
     (finally (stop-scissor-test))))

(def ^{:private true :doc "the named colour"}
  named-colours {:white [1.0 1.0 1.0] :black [0.0 0.0 0.0]})

(defn clear-colour
  "sets the clear colour"
  ([r g b a]
     {:pre (every? float [r g b a])}
     (GL11/glClearColor r g b a))
  ([r g b]
     (clear-colour r g b 0.0))
  ([colour]
     {:pre (colour named-colours)}
     (apply clear-colour (colour named-colours))))

(defn clear-colour-buffer
  "clears the colour buffer"
  []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))

(def ^{:private true :doc "polygon modes for glBegin"} polygon-modes
  {:points GL11/GL_POINTS
   :lines GL11/GL_LINES
   :line-strip GL11/GL_LINE_STRIP
   :line-loop GL11/GL_LINE_LOOP
   :triangles GL11/GL_TRIANGLES
   :triangle-strip GL11/GL_TRIANGLE_STRIP
   :triangle-fan GL11/GL_TRIANGLE_FAN
   :quads GL11/GL_QUADS
   :quad-strip GL11/GL_QUAD_STRIP
   :polygon GL11/GL_POLYGON})

(defn draw-shape
  "draws a shape in immediate mode"
  [polygon-mode & vertices]
  {:pre [(polygon-mode polygon-modes) (not (empty? vertices)) (every? vector? vertices)]}
  (GL11/glBegin (polygon-mode polygon-modes))
  (doseq [[x y] vertices]
    (GL11/glVertex2f x y))
  (GL11/glEnd))

(defn display-list-generate
  "generate a display list from a function to draw the contents of the list"
  [draw-fn]
  {:pre (fn? draw-fn)}
  (let [lidx (GL11/glGenLists 1)]
    (GL11/glNewList lidx GL11/GL_COMPILE)
    (draw-fn)
    (GL11/glEndList)
    lidx))

(defn display-list-call
  "calls a previously generated display list"
  [display-list]
  {:pre (> display-list 0)}
  (GL11/glCallList display-list))

(defn load-identity
  "loads the identity matrix"
  [] (GL11/glLoadIdentity))

(defn colour 
  "sets the rendering colour"
  ([r g b a]
     {:pre (every? float? [r g b a])}
     (GL11/glColor4f r g b a))
  ([r g b]
     {:pre (every? float? [r g b])}
     (GL11/glColor3f r g b))
  ([colour]
     {:pre (colour named-colours)}
     (apply colour (colour named-colours))))

(defn translate
  "performs a translation call"
  ([x y z]
     (GL11/glTranslatef x y z))
  ([x y]
     (translate x y 0)))

(defmacro with-pushed-matrix
  "performs body within a push and then a pop matrix"
  [& body]
  `(try
     (GL11/glPushMatrix)
     ~@body
     (finally (GL11/glPopMatrix))))

(defn get-mouse-pos 
  "gets the mouse position"
  [] 
  [(Mouse/getX) (Mouse/getY)])
