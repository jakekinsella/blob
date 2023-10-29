(ns notes.components.editor
  (:require
    [clojure.math :as math]
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
   :padding-top "58px"
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

(defonce width (r/atom nil))
(defonce height (r/atom nil))
(defonce pressed (r/atom false))
(defonce points (r/atom []))

(defclass canvas-style []
  {:background "repeating-linear-gradient(white, white 60px, #777 61px)"})
(defn canvas-editor []
  (let [ref (react/useRef)
        drawing @(re-frame/subscribe [::subs/drawing])]
    (do (react/useEffect (fn []
          (let [ctx (-> ref .-current (.getContext "2d"))
                draw-line (fn [drawing points]
                             (do (set! (.-lineCap ctx) "round")
                                 (set! (.-lineJoin ctx) "round")
                                 (if (= drawing "pen")
                                   (do (set! (.-lineWidth ctx) 6)
                                       (set! (.-globalCompositeOperation ctx) "source-over")
                                       (set! (.-strokeStyle ctx) "black"))
                                   (do (set! (.-lineWidth ctx) 15)
                                       (set! (.-globalCompositeOperation ctx) "destination-out")
                                       (set! (.-strokeStyle ctx) "rgba(255,255,255,1)")))
                                 (.beginPath ctx)
                                 (dorun
                                   (map
                                     (fn [[from to]]
                                       (let [xc (/ (+ (:x from) (:x to)) 2)
                                             yc (/ (+ (:y from) (:y to)) 2)]
                                         (if (= drawing "pen")
                                             (if (or (nil? (:pressure from)) (= (:pressure from) 0))
                                               (set! (.-lineWidth ctx) 6)
                                               (set! (.-lineWidth ctx) (* 15 (math/log (+ 1 (:pressure from)))))))
                                         (.quadraticCurveTo ctx (:x from) (:y from) xc yc)))
                                     (map vector points (rest points))))
                                 (.stroke ctx)))
                save (fn [] (re-frame/dispatch [::events/save-note @title @body]))
                add-point (fn [e]
                            (if (.-isPrimary e)
                              (let [canvas (.-current ref)
                                    rect (.getBoundingClientRect canvas)
                                    scale-x (/ (.-width canvas) (.-width rect))
                                    scale-y (/ (.-height canvas) (.-height rect))
                                    x (* (- (.-clientX e) (.-left rect)) scale-x)
                                    y (* (- (.-clientY e) (.-top rect)) scale-y)
                                    last (last @points)]
                                  (if (or (nil? last) (> (Math/sqrt (+ (Math/pow (- (:x last) x) 2) (Math/pow (- (:y last) y) 2))) 2))
                                    (reset! points (concat @points [{:x x :y y :pressure (.-pressure e)}]))))))
                draw (fn [e]
                       (if @pressed
                         (do (add-point e)
                             (draw-line drawing @points))))
                mousedown (fn [e]
                  (if (.-isPrimary e)
                      (do (reset! pressed (not (= (.-pointerType e) "touch")))
                          (draw e))))
                touch (fn [e] (if @pressed (.preventDefault e)))
                mouseup (fn [e]
                  (do (set! (-> js/document .-body .-style .-userSelect) "auto")
                      (reset! pressed false)
                      (reset! body (assoc @body :lines (concat (:lines @body) [{:drawing drawing :points @points}])))
                      (save)
                      (reset! points [])))
                scroll (fn [e]
                  (let [screen-height (-> js/window .-screen .-height)
                        canvas-height (:height @body)
                        y (.-scrollY js/window)]
                    (if (>= y (- canvas-height screen-height))
                      (do (reset! body (assoc @body :height (+ canvas-height screen-height)))
                          (reset! height (+ canvas-height screen-height))
                          (save)))))
                mount (fn []
                        (do (set! (-> ref .-current .-width) (math/ceil (* 1.5 (:width @body))))
                            (set! (-> ref .-current .-height) (math/ceil (* 1.5 (:height @body))))
                            (set! (-> ref .-current .-style .-width) (str (:width @body) "px"))
                            (set! (-> ref .-current .-style .-height) (str (:height @body) "px"))
                            (dorun (map (fn [line] (draw-line (:drawing line) (:points line))) (:lines @body)))))]

            (do (mount)
                (js/document.addEventListener "pointermove" draw)
                (js/document.addEventListener "pointerdown" mousedown)
                (js/document.addEventListener "touchstart" touch (clj->js {:passive false}))
                (js/document.addEventListener "pointerup" mouseup)
                (js/document.addEventListener "scroll" scroll)
                (fn [] (do (js/document.removeEventListener "pointermove" draw)
                           (js/document.removeEventListener "pointerdown" mousedown)
                           (js/document.removeEventListener "touchstart" touch)
                           (js/document.removeEventListener "pointerup" mouseup)
                           (js/document.removeEventListener "scroll" scroll)))))))

      [:canvas {:class (canvas-style) :ref ref :width @width :height @height}])))

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (if (not (= (:title selected) @title))
      (do (if (and (not (nil? @title)) (not (string? (:body selected)))) (-> js/window .-location .reload))
          (reset! title (:title selected))
          (reset! body (:body selected))
          (reset! width (:width @body))
          (reset! height (:width @body))
          (reset! pressed false)
          (reset! points [])))
    (if (not (string? @body)) [:f> canvas-editor] [text-editor])))
