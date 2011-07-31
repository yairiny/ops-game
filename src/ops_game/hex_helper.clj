(ns ops-game.hex-helper)

(def ^{:private true :doc "hex direction coordinate offsets"}
  hex-dir-offsets
  {:even {:left [0 -1] :up-left [-1 -1] :up-right [-1 0] :right [0 1] :down-right [1 0] :down-left [1 -1]}
   :odd {:left [0 -1] :up-left [-1 0] :up-right [-1 1] :right [0 1] :down-right [1 1] :down-left [1 0]}})

(defn loc? 
  "returns true if x is a valid hex location"
  [x] 
  (and (vector? x) (= 2 (count x)) (every? #(>= % 0) x)))

(defn- next-hex
  "returns the coordinates of the next hex by direction"
  [hex-loc direction]
  (let [[row col] hex-loc
        offsets (hex-dir-offsets (if (even? row) :even :odd))]
    (vec (map + hex-loc (direction offsets)))))

(defn get-adjacent
  "gets the vector of adjacent hexes"
  [loc num-rows num-cols]
  {:pre (loc? loc)}
  (let [all-adj (map (partial next-hex loc) [:left :up-left :up-right :right :down-right :down-left])]
    (filter (fn [[row col]] (and (>= row 0) (>= col 0) (<= row (dec num-rows)) (<= col (dec num-cols)))) all-adj)))

(defn- norm-diff
  "returns a normalised difference between two locations, the magnitude is always 1 but the sign is
as per the real difference.  e.g. [3 3] [0 7] returns [1 -1]"
  [[r1 c1] [r2 c2]]
  [(if (> r1 r2) 1 (if ( < r1 r2) -1 0)) (if (> c1 c2) 1 (if (< c1 c2) -1 0))])

(defn hex-dist
  "returns the hex distance between two locations - the minimum number of steps to get from a to b"
  [a b]
  {:pre (every? loc? [a b])}
  (println a)
  (if (= a b) 0
      (let [[r c] a
            [dr dc] (norm-diff b a)
            moves (vals (hex-dir-offsets (if (even? r) :even :odd)))
            move-to-make (if (some #{[dr dc]} moves) [dr dc] [dr 0])]
        (inc (hex-dist (vec (map + a move-to-make)) b)))))

(defn reachable-hexes
  "returns all reachable locations from a source location and the best path given a cost function"
  ([loc acc path potential cost-fn]
     (if (or (nil? (acc loc)) (> potential (:potential (acc loc))))
       (let [acc (assoc acc loc {:potential potential :path path})
             adj (get-adjacent loc 1000 1000)
             path (conj path loc)]
         (reduce (fn [acc loc]
                   (let [cost (cost-fn loc)]
                     (when (nil? cost) (println loc))
                     (if (< potential cost) acc
                         (reachable-hexes loc acc path (- potential cost) cost-fn))))
                 acc adj))
       acc))
  ([loc potential cost-fn]
     {:pre [(loc? loc) (fn? cost-fn) (>= potential 0)]}
     (reachable-hexes loc {} [] potential cost-fn)))
