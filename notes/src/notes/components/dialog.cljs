(ns notes.components.dialog
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass floating-style []
  {:position "absolute"
   :width "100%"
   :height "100%"
   :display "flex"
   :align-items "center"
   :justify-content "center"
   :vertical-align "middle"
   :z-index 11})
(defn floating [children] (into [:div {:class (floating-style)}] children))

(defclass card-style []
  {:width "300px"
   :height "200px"
   :background-color central/Constants.Colors.Container.background
   :padding-top "20px"
   :padding-bottom "20px"
   :padding-left "30px"
   :padding-right "30px"
   :border (str "1px solid" central/Constants.Colors.Container.border)
   :border-radius "5px"
   :box-shadow (str "0px 0px 1px" central/Constants.Colors.Container.shadow)})
(defn card [children] (into [:div {:class (card-style)}] children))

(defclass title-style []
  {:padding-top "5px"
   :padding-bottom "25px"
   :font-size "22px"}
  (at-media {:max-width "750px"}
    {:font-size "24px"}))
(defn title [child] [:div {:class (title-style)} child])

(defclass label-style [] {:padding-bottom "3px"})
(defn label [child] [:div {:class (label-style)} child])

(defclass spacer-style [] {:height "10px"})
(defn spacer [] [:div {:class (spacer-style)}])

(defclass error-label-style []
  {:height "20px"
   :font-size "14px"}
  (at-media {:max-width "750px"}
    {:font-size "16px"}))
(defn error-label [child] [:div {:class (error-label-style)} child])

(defclass textbox-style []
  {:display "block"
   :box-sizing "border-box"
   :width "100%"
   :height "35px"
   :padding-left "10px"
   :padding-right "10px"
   :background "transparent"
   :color central/Constants.Colors.Text.base
   :border (str "1px solid" central/Constants.Colors.Container.border)
   :border-radius "3px"
   :font-size "15px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"}
  [:&:focus {:outline "none"}]
  (at-media {:max-width "750px"}
    {:font-size "17px"}))
(defn textbox [attrs]
  [:input (merge-with + {:class (textbox-style) :type "text" :required true} attrs)])

(defclass submit-style []
  {:width "100%"
   :height "40px"
   :cursor "pointer"
   :border (str "1px solid" central/Constants.Colors.Container.border)
   :border-radius "5px"
   :background-color central/Constants.Colors.Container.background
   :font-size "18px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"
   :color central/Constants.Colors.Text.base}
  [:&:hover {:background-color central/Constants.Colors.Text.Inverted.hover}]
  [:&:active {:background-color central/Constants.Colors.Text.Inverted.active}]
  (at-media {:max-width "750px"}
    {:font-size "20px"}))
(defn submit [child] [:button {:class (submit-style)} child])

(def value (r/atom ""))
(defn build []
  (let [dialog @(re-frame/subscribe [::subs/dialog])
        error @(re-frame/subscribe [::subs/error])]
    (if (nil? dialog)
        (do (reset! value "") ())
        (let [on-submit (fn [event]
                            (.preventDefault event)
                            ((:on-submit dialog) @value))]
          (floating
            [[:div
              {:on-click (fn [event] (.stopPropagation event))}
              (card [(title (:title dialog))
                     [:form
                       {:on-submit on-submit}
                       (label (:label dialog))
                       (spacer)
                       (textbox {:type "text" :value @value :on-change #(reset! value (-> % .-target .-value))})
                       (error-label error)
                       (submit (:submit dialog))
                       [:input {:type "submit" :style {:display "none"}}]]])]])))))
