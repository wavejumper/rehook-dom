(ns rehook.dom)

(defn eval-hiccup
  ([$ e]
   ($ e))
  ([$ e props]
   ($ e props))
  ([$ e props & children]
   (apply $ e props (map (fn [x]
                           (if (vector? x)
                             (apply eval-hiccup $ x)
                             x))
                         children))))

(declare compile-hiccup)

#?(:clj
   (defmacro html [$ component]
     (if (vector? component)
       `~(apply compile-hiccup $ component)
       `(apply eval-hiccup ~$ ~component))))

(defn compile-hiccup
  ([$ e]
   (list $ e))
  ([$ e props]
   (list $ e props))
  ([$ e props & children]
   (apply list $ e props (map (fn [x]
                                (cond
                                  (vector? x) (apply compile-hiccup $ x)
                                  (string? x) x
                                  (number? x) x
                                  :else `(html ~$ ~x)))
                              children))))

#?(:clj
   (defmacro defui
     [name [ctx props $] & body]
     `(def ~name
        ^:rehook/component
        (fn [ctx# $#]
          (let [~ctx ctx#
                ~$ $#]
            (fn ~(gensym name) [props#]
              (let [~props props#]
                ~@body)))))))

#?(:clj
   (defmacro ui
     [[ctx props $] & body]
     `(with-meta
       (fn [ctx# $#]
         (let [~ctx ctx#
               ~$ $#]
           (fn [props#]
             (let [~props props#]
               ~@body))))
       {:rehook/component true})))

