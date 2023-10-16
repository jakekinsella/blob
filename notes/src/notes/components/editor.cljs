(ns notes.components.editor
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [react :as react]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass textbox-style []
  {:width "98%"
   :height "93%"
   :padding-left "1%"
   :padding-right "1%"
   :border "none"
   :font-size "15px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"
   :user-select "none"
   :resize "none"}
  [:&:focus {:outline "none"}]
  (at-media {:max-width "750px"}
    {:font-size "17px"}))
(defn textbox [attrs]
  [:textarea (merge-with + {:class (textbox-style) :required true} attrs)])

(defonce title (r/atom nil))
(defonce body (r/atom nil))

(defn text-editor []
  [textbox {:value @body
            :on-change (fn [event]
                         (let [value (-> event .-target .-value)]
                           (reset! body value)
                           (re-frame/dispatch [::events/save-note @title value])))}])

(defonce pressed (r/atom false))
(defonce points (r/atom []))

(defclass canvas-style []
  {:background "repeating-linear-gradient(white, white 35px, #777 36px)"})
(defn canvas-editor []
  (let [ref (react/useRef)
        drawing @(re-frame/subscribe [::subs/drawing])]
    (do (react/useEffect (fn []
          (let [ctx (-> ref .-current (.getContext "2d"))
                draw-line (fn [drawing points]
                             (do (set! (.-lineCap ctx) "round")
                                 (set! (.-lineJoin ctx) "round")
                                 (if (= drawing "pen")
                                   (do (set! (.-lineWidth ctx) 3)
                                       (set! (.-globalCompositeOperation ctx) "source-over")
                                       (set! (.-strokeStyle ctx) "black"))
                                   (do (set! (.-lineWidth ctx) 15)
                                       (set! (.-globalCompositeOperation ctx) "destination-out")
                                       (set! (.-strokeStyle ctx) "rgba(255,255,255,1)")))
                                 (.beginPath ctx)
                                 (dorun (map (fn [[from to]]
                                           (.moveTo ctx (:x from) (:y from))
                                           (.lineTo ctx (:x to) (:y to))
                                           (.stroke ctx))
                                         (map vector points (rest points))))))
                save (fn [] (re-frame/dispatch [::events/save-note @title @body]))
                add-point (fn [e]
                            (let [canvas (.-current ref)
                                  rect (.getBoundingClientRect canvas)
                                  scale-x (/ (.-width canvas) (.-width rect))
                                  scale-y (/ (.-height canvas) (.-height rect))
                                  x (* (- (.-clientX e) (.-left rect)) scale-x)
                                  y (* (- (.-clientY e) (.-top rect)) scale-y)]
                                (reset! points (concat @points [{:x x :y y}]))))
                draw (fn [e]
                       (if @pressed
                         (do (add-point e)
                             (draw-line drawing @points))))
                mousedown (fn [e] (reset! pressed true) (draw e))
                mouseup (fn [e]
                  (do (reset! pressed false)
                      (reset! body (assoc @body :lines (concat (:lines @body) [{:drawing drawing :points @points}])))
                      (save)
                      (reset! points [])))
                scroll (fn [e]
                  (let [screen-height (-> js/window .-screen .-height)
                        canvas-height (:height @body)
                        y (.-scrollY js/window)]
                    (if (>= y (- canvas-height screen-height))
                      (do (reset! body (assoc @body :height (+ canvas-height screen-height)))
                          (save)))))
                init (fn []
                       (do (set! (-> ref .-current .-width) (* (:width @body) 1.5))
                           (set! (-> ref .-current .-height) (* (:height @body) 1.5))
                           (set! (-> ref .-current .-style .-width) (str (:width @body) "px"))
                           (set! (-> ref .-current .-style .-height) (str (:height @body) "px"))
                           (dorun (map (fn [line] (draw-line (:drawing line) (:points line))) (:lines @body)))))]

            (do (init)
                (js/document.addEventListener "mousemove" draw)
                (js/document.addEventListener "mousedown" mousedown)
                (js/document.addEventListener "mouseup" mouseup)
                (js/document.addEventListener "scroll" scroll)
                (fn [] (do (js/document.removeEventListener "mousemove" draw)
                           (js/document.removeEventListener "mousedown" mousedown)
                           (js/document.removeEventListener "mouseup" mouseup)
                           (js/document.removeEventListener "scroll" scroll)))))))

      [:canvas {:class (canvas-style) :ref ref :width (:width @body) :height (:height @body)}])))

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (if (not (= (:title selected) @title))
      (do (reset! title (:title selected))
          (reset! body (:body selected))
          (reset! pressed false)
          (reset! points [])))
    (if (string? @body) [text-editor] [:f> canvas-editor])))
