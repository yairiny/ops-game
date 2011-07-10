(ns ops-game.rendering.constants)

(def c30 (Math/cos (/ Math/PI 6)))
(def s30 0.5)

(def s 30)
(def h (* s30 s))
(def r (* c30 s))
(def a (+ r r))
(def b (+ s s))
(def b-h (- b h))

(def s* (- s 1))
(def h* (* s30 s*))
(def r* (* c30 s*))
(def a* (+ r* r*))
(def b* (+ s* s*))
(def b-h* (- b* h*))
