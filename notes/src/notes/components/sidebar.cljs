(ns notes.components.sidebar
  (:require
    [clojure.core.reducers :as reducers]
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass outer-style [] {:width "100%" :display "flex"})
(defn outer [children] (into [:div {:class (outer-style)}] children))

(defclass pane-style [] {:min-width "18vw" :z-index 10})
(defn pane [children] (into [:div {:class (pane-style)}] children))

(defclass pane-inner-style []
  {:min-width "18vw"
   :max-width "18vw"
   :min-height "100%"
   :max-height "100%"
   :position "fixed"
   :overflow-x "hidden"
   :overflow-y "scroll"
   :border-right (str "1px solid" central/Constants.colors.black)
   :background-color "white"
   :box-shadow (str "0px 0px 1px" central/Constants.colors.black)}
  (at-media {:max-width "750px"}
    {:min-width "100%"}))
(defn pane-inner [children] (into [:div {:class (pane-inner-style)}] children))

(defclass container-style [] {:padding-top "14px" :padding-bottom "50px"})
(defn container [children]
  (into [:div {:class (container-style)}] children))

(defclass header-style []
  {:padding-left "20px"
   :padding-top "5px"
   :padding-bottom "5px"
   :text-decoration "none"
   :font-size "18px"
   :color central/Constants.colors.black}
  [:&:hover {:text-decoration "none" :color "black"}]
  [:&:visited {:text-decoration "none"}]
  [:&:active {:text-decoration "none"}]
  (at-media {:max-width "750px"}
    {:font-size "22px"}))
(defn header [attrs children]
  (into [:a (merge-with + attrs {:class (header-style)})] children))

(defclass spacer-style [] {:width "100%" :height "8px"})
(defn spacer []
  [:div {:class (spacer-style)}])

(defclass item-style []
  {:display "block"
   :padding-left "30px"
   :text-decoration "none"
   :font-size "15px"
   :color central/Constants.colors.black
   :cursor "pointer"}
  [:&:hover {:text-decoration "none" :color "black"}]
  [:&:visited {:text-decoration "none"}]
  [:&:active {:text-decoration "none"}]
  (at-media {:max-width "750px"}
    {:font-size "18px" :padding-top "7px"}))
(defn item [attrs children]
  (into [:a (merge-with + attrs {:class (item-style)})]
    children))

(defclass spacer2-style [] {:height "10px"})
(defn spacer2 [] [:div {:class (spacer2-style)}])

(defclass folder-title-style []
  {:margin-left "10px"
  	:margin-bottom "3px"
  	:font-size "16px"
  	:user-select "none"
   :cursor "pointer"}
  [:&:hover {:color "black"}]
  [:&:active {:color "black"}]
  (at-media {:max-width "750px"}
    {:font-size "19px" :padding-top "7px"}))
(defn folder-title [attrs children]
  (into [:div (merge-with + attrs {:class (folder-title-style)})]
    children))

(defclass top-style []
  {:display "flex"})
(defn top [children] (into [:div {:class (top-style)}] children))

(defclass right-style []
  {:padding-top "7px"
   :margin-left "auto"
   :margin-right "20px"
   :cursor "pointer"}
   [:&:hover {:color "black"}]
   [:&:active {:color "black"}]
   (at-media {:max-width "750px"}
    {:padding-right "10px"}))
(defn right [attrs children] (into [:div (merge-with + attrs {:class (right-style)})] children))


(defonce collapsed (r/atom (set [])))

(defn build [children]
  (let [render-item (fn [note]
                      [item {:href (str "/notes/" (js/encodeURIComponent (:title note)))
                             :on-click (fn [] (if (<= (-> js/window .-innerWidth) 750) (re-frame/dispatch [::events/sidebar-close])))
                             :key (:title note)}
                            [(:short-title note)]])
  						render-items (fn [notes]
                       (let [by-folder (reducers/reduce
                                         (fn [acc note]
                                           (let [folder (:folder note)]
                                           (assoc acc (:folder note) (concat (get acc folder) [note]))))
                                         {} notes)]
                         (doall
                           (map (fn [[folder notes]]
                         		       (if (contains? @collapsed folder)
                         		         [:div {:key folder}
                                  	     (folder-title {:on-click (fn [] (reset! collapsed (disj @collapsed folder)))} folder)]
                         		         [:div {:key folder}
                                  	     (folder-title {:on-click (fn [] (reset! collapsed (conj @collapsed folder)))} folder)
                                  	     (map render-item notes)
                                  	     [spacer2]]))
                         		     by-folder))))

  						render-add (fn []
                     (let [dialog {:title "Add Note"
                                   :label "Title"
                                   :submit "Save"
                                   :on-submit (fn [value] (re-frame/dispatch [::events/save-note value "" [::events/dialog-close]]))}]
                       (item {:href "#" 
                              :on-click (fn [event] 
                                          (do (.stopPropagation event) (re-frame/dispatch [::events/dialog-open dialog])))} "+ Add note")))
        render-add-drawing (fn []
                             (let [default {:type "drawing" :width (-> js/window .-screen .-width) :height (-> js/window .-screen .-height (* 2)) :lines []}
                                   dialog {:title "Add Drawing"
                                           :label "Title"
                                           :submit "Save"
                                           :on-submit (fn [value] (re-frame/dispatch [::events/save-note value default [::events/dialog-close]]))}]
                               (item {:href "#" 
                                      :on-click (fn [event] 
                                                  (do (.stopPropagation event) (re-frame/dispatch [::events/dialog-open dialog])))} "+ Add drawing")))
  						render-sidebar (fn []
                         (let [notes @(re-frame/subscribe [::subs/notes])]
                           (pane
                             [(pane-inner
                               [(container
                                 [(top
                                   [(header {:href "/"} "Notes")
                                    (right {:on-click (fn [] (re-frame/dispatch [::events/sidebar-close]))} [[:> central/Icon {:icon "arrow_back_ios_new" :size "1em"}]])])
                                  (spacer)
                                  [:div (render-add) (render-add-drawing) (spacer) (render-items notes)]])])])))]

  (let [sidebar-open? @(re-frame/subscribe [::subs/sidebar-open?])]
    (into (outer [(if sidebar-open? (render-sidebar) [:div])]) children))))
