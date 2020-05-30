(ns mandelbrot.core)

(def max-iterations 100)

(defn ->complex
      [real imaginary]
      [real imaginary])


(defn complex+
      [[r1 i1] [r2 i2]]
      [(+ r1 r2) (+ i1 i2)])

(defn complex*
      [[r1 i1] [r2 i2]]
      [(- (* r1 r2)
          (* i1 i2))
       (+ (* r1 i2)
          (* i1 r2))])

(defn smallish?
      [[r i]]
      #_(> 2 (Math/sqrt (+ (* r r) (* i i))))
      ;; TODO need to actually check if this is more performant
      (> 4 (Math/abs (+ (* r r) (* i i)))))

(defn iterations
      [c]
      (->> [0 0]
           (iterate #(complex+ (complex* % %) c))
           (take-while smallish?)
           (take max-iterations)))

(defn translate
      "Translate a value to be within the bounds provided, or -2 < x < 2,
      given the size of canvas"
      #_([pos size]
         (- (* 4 (/ pos size)) 2))
      ([pos size lower-bound upper-bound]
       (-> pos
           (/ size)
           (* (- upper-bound lower-bound))
           (+ lower-bound)))
      ([pos size]
       (translate pos size -2 2)))

(def random-golden-ratio-colours
  (->> (rand 360)
       (iterate #(+ 222.5 %))
       (map #(str "hsl(" % ",100%,50%)"))
       (take max-iterations)
       (vec)
       (#(conj % "rgb(0,0,0)"))))

(def gradient-colours
  (->> 0
       (iterate #(+ 3 %))
       (map #(str "hsl(" % ",100%,50%)"))
       (take max-iterations)
       (vec)
       (#(conj % "rgb(0,0,0)"))))

;; Change this to switch color scheme easily
(def colour-scheme gradient-colours)

(def canvas (.getElementById js/document "mandelbrot"))

(def ctx (.getContext canvas "2d"))
(def bounds (atom {:x-lower -2 :x-upper 2
                   :y-lower -2 :y-upper 2}))

(defn render
      "Attempt to render the Mandelbrot set"
      ([x-lower-bound x-upper-bound
        y-lower-bound y-upper-bound]
       (doseq [x (range (.-width canvas))
               y (range (.-height canvas))
               :let [xs (translate x (.-width canvas) x-lower-bound x-upper-bound)
                     ys (translate y (.-height canvas) y-lower-bound y-upper-bound)
                     its (count (iterations [xs ys]))
                     colour (nth colour-scheme its)]]
              (set! (.-fillStyle ctx) colour)
              (.fillRect ctx x y 1 1)))
      ([]
       (let [{:keys [x-lower x-upper
                     y-lower y-upper]} @bounds]
            (render x-lower x-upper y-lower y-upper))))

(defn handle-click
      [event]
      (let [click-x (.-offsetX event)
            click-y (.-offsetY event)
            {:keys [x-lower x-upper
                    y-lower y-upper]} @bounds
            xs (translate click-x (.-width canvas) x-lower x-upper)
            ys (translate click-y (.-height canvas) y-lower y-upper)
            x-step (/ (- x-upper x-lower) 4)
            y-step (/ (- y-upper y-lower) 4)]
           (reset! bounds {:x-lower (- xs x-step) :x-upper (+ xs x-step)
                           :y-lower (- ys y-step) :y-upper (+ ys y-step)})
           (render)))

(.addEventListener canvas "click" handle-click)