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

(defn canvas-editor []
  (let [ref (react/useRef)
        drawing @(re-frame/subscribe [::subs/drawing])]
    (do (react/useEffect (fn []
          (let [ctx (-> ref .-current (.getContext "2d"))
                apply-line (fn []
                             (if (= drawing :pen)
                               (do (set! (.-lineWidth ctx) 1)
                                   (set! (.-strokeStyle ctx) "black"))
                               (do (set! (.-lineWidth ctx) 10)
                                   (set! (.-strokeStyle ctx) "white"))))
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
                             (set! (.-lineCap ctx) "round")
                             (set! (.-lineJoin ctx) "round")
                             (apply-line)
                             (.beginPath ctx)
                             (dorun (map (fn [[from to]]
                                           (.moveTo ctx (:x from) (:y from))
                                           (.lineTo ctx (:x to) (:y to))
                                           (.stroke ctx))
                                         (map vector @points (rest @points)))))))
                mousedown (fn [e] (reset! pressed true) (draw e))
                mouseup (fn [e] (reset! pressed false) (reset! points []))
                scroll (fn [e]
                  (let [screen-height (-> js/window .-screen .-height)
                        canvas-height (:height @body)
                        y (.-scrollY js/window)]
                    (if (>= y (- canvas-height screen-height))
                      (do (reset! body (assoc @body :height (+ canvas-height screen-height)))
                          (save)))))]
            (do (js/document.addEventListener "mousemove" draw)
                (js/document.addEventListener "mousedown" mousedown)
                (js/document.addEventListener "mouseup" mouseup)
                (js/document.addEventListener "scroll" scroll)
                (fn [] (do (js/document.removeEventListener "mousemove" draw)
                           (js/document.removeEventListener "mousedown" mousedown)
                           (js/document.removeEventListener "mouseup" mouseup)
                           (js/document.removeEventListener "scroll" scroll)))))))

      [:canvas {:ref ref :width (:width @body) :height (:height @body)}])))

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (if (not (= (:title selected) @title))
      (do (reset! title (:title selected))
          (reset! body (:body selected))
          (reset! pressed false)
          (reset! points [])))
    (if (string? @body) [text-editor] [:f> canvas-editor])))
