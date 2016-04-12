package org.codelibs.elasticsearch.dynarank.filter;

import org.codelibs.elasticsearch.dynarank.ranker.DynamicRanker;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.tasks.Task;

public class SearchActionFilter extends AbstractComponent
        implements ActionFilter {

    private int order;

    private DynamicRanker dynamicRanker;

    @Inject
    public SearchActionFilter(final Settings settings) {
        super(settings);

        order = settings.getAsInt("indices.dynarank.filter.order", 10);
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public void apply(final Task task, final String action,
            @SuppressWarnings("rawtypes") final ActionRequest request,
            @SuppressWarnings("rawtypes") final ActionListener listener,
            final ActionFilterChain chain) {
        if (!SearchAction.INSTANCE.name().equals(action)) {
            chain.proceed(task, action, request, listener);
            return;
        }

        final SearchRequest searchRequest = (SearchRequest) request;
        @SuppressWarnings("unchecked")
        final ActionListener<SearchResponse> wrappedListener = dynamicRanker
                .wrapActionListener(action, searchRequest, listener);
        chain.proceed(task, action, request,
                wrappedListener == null ? listener : wrappedListener);
    }

    @Override
    public void apply(final String action, final ActionResponse response,
            @SuppressWarnings("rawtypes") final ActionListener listener,
            final ActionFilterChain chain) {
        chain.proceed(action, response, listener);
    }

    public void setDynamicRanker(final DynamicRanker dynamicRanker) {
        this.dynamicRanker = dynamicRanker;
    }

}
