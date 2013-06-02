/**
 * Entry point for application.
 * Here is all the basic setups happened, library linked and AMD implemented.
 * Author: bearz
 */
require.config({
    // url where all the js placed
    baseUrl: "js/",
    // relative names for "define" used in apps. Easy changing of versions
    paths: {
        jquery: 'libs/jquery-1.10.0',
        underscore: 'libs/underscore-1.4.4',
        backbone: 'libs/backbone-1.0.0',
        bootstrap: 'libs/bootstrap-2.3.2',
        base_view: 'views/base_view',
        log: 'libs/loglevel-0.2.0'

    },
    // Trick for making apps js complaint. See more: http://requirejs.org/docs/api.html#config-shim
    shim: {
        underscore: {
            exports: "_"
        },
        backbone: {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        },
        bootstrap: {
            deps: ['jquery']
        },
        enforceDefine: true
    }
});

require([
    'backbone',
    'jquery',
    'underscore',
    'views/app_view',
    'log'
], function (Backbone, $, _, AppView, log) {
    var Router = Backbone.Router.extend({
        initialize: function() {
            this.app_view = new AppView();
        },
        routes: {
            "": "start",

            "*notFound": "notFound" // reaction to unknown URL.
        },

        start: function() {
            this.app_view.render(); // start rendering app view
        },
        notFound: function() {
            window.alert("Unknown URL: " + window.location.href);
        }
    });

    log.setLevel("DEBUG");
    log.debug("Starting tellmemore client application");
    var router = new Router(); // always create router before history start
    Backbone.history.start();
});
