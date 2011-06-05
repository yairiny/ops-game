(ns ops-game.test.data
  (:require [ops-game.data :as data] :reload)
  (:use [clojure.test]))

(defn- small-game-map []
  (repeat 4 (vec (repeat 4 :plain))))

(defn- make-test-unit [loc]
  (assoc (@#'data/make-unit :infantry "full name" "name" 5 0) :location loc))

(defn- single-unit []
  (let [units {100 (make-test-unit [1 1])}]
    {:units-by-loc (@#'data/make-units-by-loc units) :units units}))

(defn- two-units []
  (let [units {100 (make-test-unit [1 1]), 101 (make-test-unit [1 1])}]
    {:units-by-loc (@#'data/make-units-by-loc units) :units units}))

(defmacro with-game-data-bindings [units map & body]
  `(do (let [test-units# ~units]
         (binding [data/game-map ~map
                   data/units (atom (:units test-units#))
                   data/units-by-loc (atom (:units-by-loc test-units#))
                   data/selected-unit (atom nil)]
           ~@body))))

(defmacro assert-selected [sexp]
  (let [sexps# (concat `(~@sexp) `[@@~#'data/selected-unit])]
    `(is ~sexps#)))

(deftest moving-a-single-unit
  (with-game-data-bindings (single-unit) (small-game-map)
    (data/update-unit-selected [1 1])
    (data/move-selected-unit [2 2])
    (is (= [2 2] (-> @@#'data/units (get 100) :location)))
    (is (-> @@#'data/units-by-loc (get [2 2]) :units (= #{100})))))

(deftest selecting-a-unit-that-is-single
  (with-game-data-bindings (single-unit) (small-game-map)
    (assert-selected (nil?))
    (data/update-unit-selected [1 1])
    (assert-selected (= 100))))

(deftest selecting-multiple-units
  (with-game-data-bindings (two-units) (small-game-map)
    (assert-selected (nil?))
    (data/update-unit-selected [1 1])
    (assert-selected (= 100))
    (data/update-unit-selected [1 1])
    (assert-selected (= 101))
    (data/update-unit-selected [1 1])
    (assert-selected (= 100))))

(deftest clicking-a-hex
  (is (nil? @@#'data/hex-clicked))
  (update-hex-clicked [1 1])
  (is (= [1 1] @@#'data/hex-clicked)))
