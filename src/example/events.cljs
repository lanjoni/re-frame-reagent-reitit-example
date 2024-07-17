(ns example.events
  (:require
    [example.db :as db]
    [example.effects :as effects]
    [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-fx
  ::navigate
  (fn [db [_ route]]
    ;; See `navigate` effect in routes.cljs
    {::effects/navigate! route}))

(re-frame/reg-event-db
  ::navigated
  (fn [db [_ new-match]]
    (assoc db :current-route new-match)))
