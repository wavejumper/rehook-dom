# rehook-dom

[![Clojars Project](https://img.shields.io/clojars/v/wavejumper/rehook-rn-component.svg)](https://clojars.org/wavejumper/rehook-rn-component)

React component micro-library for Clojurescript

The core namespace is only 30LOC. It makes Clojurescript development with React a joy!

#### Hello world

```clojure
(ns demo 
  (:require 
    [rehook.dom :refer-macros [defui]]
    [react.dom.browser :as dom.browser]
    ["react-dom" :as react-dom]))

(defn system [] ;; <-- system map (this could be integrant, component, etc)
  {:dispatch #(js/console.log "TODO: implement" %)})

(defui my-component 
  [{:keys [dispatch]} ;; <-- context map returned from bootstrap fn
   props ;; <-- any props passed from parent component
   $] ;; <-- the render fn
  ($ :Text {:onClick #(dispatch :fire-missles)} "Hello world"))

(react-dom/render 
  (dom.browser/bootstrap 
    (system) ;; <-- context map
    identity ;; <-- context transformer
    my-component) ;; <-- root component
  (js/document.getElementById "myapp"))
```

## Rehook?

[rehook](https://github.com/wavejumper/rehook/) is a Clojurescript library for state management in React apps. 

It is a simple, 35LOC library that provides a [reagent](https://github.com/reagent-project/reagent) like interface for modern Cljs/React apps.

You do not need to use `rehook-dom` with `rehook`, but the two obviously pair great! 

## Why rehook-dom?

#### A baggage free way to pass down application context to components

Maybe you want to use [integrant](https://github.com/weavejester/integrant) or [component](https://github.com/stuartsierra/component) on the front end? 

One of the biggest downfalls to cljs development is the global singleton state design adopted by many libraries. 

Eg, `re-frame` becomes cumbersome to test, or even run multiple instances of (think devcards use case) because of this pattern.

This is generally a trade-off between convenience and 'pureness'.

However `rehook-dom` gives you both! 

Via clever partial function application, the resulting DSL means you don't have to think about passing around a context map at all!

And because all `rehook-dom` components are plain Cljs fns where all inputs are arguments, you can easily test and reason about your code! Pure functions and all that.

#### Easy interop with the ReactJS ecosystem

* A Clojurescript developer should be able to simply `npm install my-react-library`, require it from the namespace and be on their way to happily using the library.
* A Clojurescript developer should be able to read the docs of `my-react-library` and intuitively map its props and API to Clojurescript.

Easy interop means you lose some Clojure idioms, but it keeps the API surface lean and obvious. 

#### Hiccup-like templating

While the resulting syntax is not as terse as conventional Hiccup notation, `rehook-dom` has no grammar! It's just function application.

I see the lack of syntax a PRO. There is no additional (runtime or compile time) transformation step. 
This makes the resulting DSL incredibly easy to reason about.

There is an implied grammar in other templating libraries, which can sometimes be a gotcha:
* Sablono doesn't support fragments
* Hicada expects the props argument to be a literal map 
* Sablono doesn't work with react native

I view these libraries as static artifacts of how React works in the now. 
They are not resiliant to future improvements to the React API, and some assumptions in these libraries mean they cannot support new features at all, or at a compromise.

The best library is no library at all! `rehook-dom`  suffers from none of the problems traditional 'wrapping' libraries have.

And that's because `react-dom` barely has an API surface!

The render fn is passed in as a argument to the component, so it can be overloaded. You can even write your own `bootstrap` fn, that defines how to render components! 

#### react-dom and react-native support

There shouldn't be any difference in API, except how you render or register your root component. 

If another React target added in the future, it should be as simple as adding another register fn for the target platform.

## defui 

`rehook.dom/defui` is a macro used to define `rehook` components. This macro is only syntactic sugar, as all `rehook` components are cljs fns.

`defui` takes in three arguments:

* `context`: immutable, application context
* `props`: any props passed to the component. This will be an untouched JS object.
* `$`: the render fn

```clojure
(ns demo 
  (:require [rehook.dom :refer-macros [defui]]))

(defui my-component [{:keys [dispatch]} _ $] 
  ($ :Text {:onClick #(dispatch :fire-missles)} "Hello world"))
```

The anonymous counterpart is `rehook.dom/ui`

## $

The `$` render fn provides Hiccup-like syntax for templating. 

Its signature looks like this: 
`[component args? & children]`

* The first argument is always a component. 
* The second (optional) argument are the component props.
* The third (optional) vararg are any component children

It supports component lookup in a few ways:

* All keywords are mapped to their equivilant name in the [React Native API](https://facebook.github.io/react-native/docs/activityindicator), eg `:KeyboardAvoidingView`. For the DOM, they are mapped to their [tag name string](https://reactjs.org/docs/react-api.html#createelement), eg `:div`. 
* Custom React Native components (eg, those imported from npm), can be referenced directly.
* All collections map to [React fragments](https://reactjs.org/docs/react-api.html#reactfragment). Every item in the collection must be a valid react element.

```clojure 
(ns example.components
  (:require 
    [rehook.dom :refer-macros [defui]]
    ["imported-react-component" :refer [ImportedReactComponent]]))

(defui fragment [_ _ $]
  [($ :Text "I am a fragment!")
   ($ :Text "I return multiple React elements")])

(defui button [{:keys [dispatch]} _ $]
  ($ :Button {:title "Fire missles" :onClick #(dispatch :fire-missles)}))

(defui app [_ _ $]
  ($ :View {:style #js {:flex 1}}
    ($ fragment)
    ($ button)
    ($ ImportedReactComponent)))
```

Note how the `$` render fn hides having to pass the `context` map to its children through clever partial function application!

### Props gotchas

* Props passed to `$` are always covnerted to JS maps via `clj->js`
* `rehook` does no special transformation to the keys in your props, so use `onPress` over `on-press` etc.

## Initializing

## React

You can call `react-dom/render` directly, and `bootstrap` your component:

```clojure 
(ns example.core 
  (:require 
    [examples.component :refer [app]]
    [rehook.dom.native :as dom]
    ["react-dom" :as react-dom]))

(defn system []
  {:dispatch (fn [& _] (js/console.log "TODO: implement dispatch fn..."))})

(defn main []
  (react-dom/render (dom/bootstrap (system) identity app)) (js/document.getElementById "app"))
```

## React Native

You can use the `rehook.dom.native/component-provider` fn if you directly call [AppRegistry](https://facebook.github.io/react-native/docs/appregistry)

```clojure 
(ns example.core
  (:require 
    [example.components :refer [app]]
    [rehook.dom.native :as dom]
    ["react-native" :refer [AppRegistry]]))

(defn system []
  {:dispatch (fn [& _] (js/console.log "TODO: implement dispatch fn..."))})

(defn main []
  (.registerComponent AppRegistry "my-app" (dom/component-provider (system) app))
```

Alternatively, if you don't have access to the `AppRegistry`, you can use the `rehook.dom.native/boostrap` fn instead - which will return a valid React element

## Context transformers

`component-provider` optionally takes in a context fn, which is applied each time the context map is passed to a component. It defaults to the `identity` function.

This can be incredibly useful for instrumentation, or for adding additional abstractions on top of the library (eg implementing your own data flow engine ala [domino](https://domino-clj.github.io/))

For example:

```clojure 
(defn ctx-transformer [ctx component]  
  (update ctx :log-ctx #(conj (or % []) (dom/component-name component))))

(dom/component-provider (system) ctx-transformer app)
```

# Testing

`rehook` promotes building applications with no singleton global state.
 Therefore, you can treat your components as 'pure functions', as all inputs to the component are passed in as arguments.

Testing (with React hooks) is a deeper topic that I will explore via a blog post in the coming months. Please check back!