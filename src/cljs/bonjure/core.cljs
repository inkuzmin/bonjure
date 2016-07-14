(ns bonjure.core
  (:require [reagent.core :as r]
            [bonjure.websockets :as ws]))

(defonce messages (r/atom []))
(defonce users (r/atom []))
(defonce input (r/atom nil))
(defonce focus (atom true))


(defonce uid (.-uid js/window))
(defonce username (.-username js/window))

(defn message-list []
  [:ul.messages
   (reverse (for [[i message] (map-indexed vector @messages)]
              ^{:key i}
              [:li
               [:span
                {:class-name (str (:color message) " username")
                 :on-click (fn []
                             (swap! input #(str (:username message) ", " %))
                             )}
                (:username message)
                ]
               ": "
               (:message message)
               ]))])

(defn message-input []
  (fn []
     [:div
      [:input.form-control.input
      {:type :text
       :placeholder (str "Say “bonjour!” to Bonjure, " username "!")
       :value @input
       :on-change #(reset! input (-> % .-target .-value))
       :on-key-down
       #(when (= (.-keyCode %) 13)
         (ws/send-msg! uid @input)
         (reset! input nil))}]
     [:span#clear
      {:on-click #(reset! input "")}]]
     ))

(def message-edit (with-meta message-input
                             {:component-did-update #(-> (r/dom-node %) .-firstChild .focus)
                              :component-did-mount #(-> (r/dom-node %) .-firstChild .focus)}))

(defn user-list []
  [:ul.users
   (for [[i user] (map-indexed vector @users)]
     ^{:key i}
     [:li
      {:style
       {:color
        (if (> (- (.getTime (js/Date.)) (:ping user)) 30000)
          (do
            ;(js/console.log (- (.getTime (js/Date.)) (:ping user)))
            "#747369")
          "#00853e")

        }
       }
      [:span
       {:class-name (str (:color user) " username")
        :on-click (fn []
                    (swap! input #(str (:username user) ", " %))
                    )}
       (:username user)]])])



(.setInterval js/window
              (fn []
                (ws/send-ping!)) 30000)

;(ws/send-notification! username)

(defn home-page []
  [:div
   [:navbar.navbar-default.navbar-fixed-top
    [:div.container
     [:div.row
      [:div.col-md-12
       [:h1 "Bon"
        [:span.c2 "jure"]]]]
     [:div.row
      [:div.col-sm-12
       [message-edit]]]]]
   [:div.container
    [:div.row
     [:div.col-sm-10
      [message-list]]
     [:div.col-sm-2.sidebar-outer
      [:div.sidebar
       "users online:"
       [user-list]
       ]]]]])

;(r/render-to-string [#'home-page])

(.addEventListener js/window "focus" (fn []
                                       (reset! focus true)
                                       (set! (.-title js/document) "Bonjure")))

(.addEventListener js/window "blur" (fn []
                                      (reset! focus false)))


(defn update-userlist! [event]
  (reset! users (:users event)))


(defn update! [event]
  (let [type (:type event)]
    (cond
      (= type :log)
      (do
        (swap! messages #(vec (into % (:messages event))));; #((:messages event)))
        )
      (= type :msg) (do
                      (when (not @focus) (set! (.-title js/document) "Bonjure – New message"))
                      (swap! messages #(vec (conj % event)))
                      ;(js/console.log "123")
                      )
      (= type :cmd)
      (update-userlist! event)

      )

    ))

(defn mount-components []
  (r/render-component [#'home-page] (.getElementById js/document "app")))


(defn init! []
  (ws/make-websocket! (str
                        (clojure.string/replace (.-protocol js/location) "http" "ws")
                        "//" (.-host js/location) "/ws") update!)
  (mount-components))
