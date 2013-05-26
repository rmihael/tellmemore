/**
 * Base view of search. Renders itself into the element that
 * parent specified for him. Allow user to search/slice and dice his
 * user database finding needed info.
 * @version prototype 0.0.1
 * Author: bearz
 */
define([
    'backbone',
    'jquery',
    'underscore',
    'base_view',
    'text!templates/search_template'
], function(Backbone, $, _, BaseView, SearchTemplate) {
    var SearchView = BaseView.extend({
        template: _.template(SearchTemplate),

        render: function() {
            this.$el.html(this.template());
        }
    });

    return SearchView;
});
