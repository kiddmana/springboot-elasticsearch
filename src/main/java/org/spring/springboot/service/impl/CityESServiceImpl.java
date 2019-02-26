package org.spring.springboot.service.impl;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.domain.City;
import org.spring.springboot.repository.CityRepository;
import org.spring.springboot.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 城市 ES 业务逻辑实现类
 *
 * Created by bysocket on 07/02/2017.
 */
@Service
public class CityESServiceImpl implements CityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CityESServiceImpl.class);

  @Autowired
  CityRepository cityRepository;

  @Autowired
  private TransportClient esClient;

  @Override
  public Long saveCity(City city) {
    if (!updateSuggest(city)) {
      return -1L;
    }
    City cityResult = cityRepository.save(city);
    return cityResult.getId();
  }

  @Override
  public List<City> searchCity(Integer pageNumber, Integer pageSize, String searchContent) {
    // 分页参数
    Pageable pageable = new PageRequest(pageNumber, pageSize);

    // Function Score Query
    FunctionScoreQueryBuilder functionScoreQueryBuilder =
        QueryBuilders.functionScoreQuery()
            .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("cityname", searchContent)), ScoreFunctionBuilders.weightFactorFunction(1000))
            .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("description", searchContent)), ScoreFunctionBuilders.weightFactorFunction(100));

    // 创建搜索 DSL 查询
    SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(pageable).withQuery(functionScoreQueryBuilder).build();

    LOGGER.info("\n searchCity(): searchContent [" + searchContent + "] \n DSL  = \n " + searchQuery.getQuery().toString());

    Page<City> searchPageResults = cityRepository.search(searchQuery);
    return searchPageResults.getContent();
  }

  /***
   * 生成联想数据
   * 
   * @param city
   * @return
   */
  public boolean updateSuggest(City city) {
    AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(esClient, AnalyzeAction.INSTANCE, "cityindex", city.getCityname());
    requestBuilder.setAnalyzer("ik_smart");
    AnalyzeResponse response = requestBuilder.get();
    List<AnalyzeToken> tokens = response.getTokens();
    System.out.println("tokens =  " + tokens.size());
    // Preconditions.checkNotNull(tokens, "Can not analyze token for house: " + city.getId());
    List<String> input = new ArrayList<String>();

    for (AnalyzeToken token : tokens) {
      System.out.println("token = " + token.getTerm());
      if (token.getTerm().length() < 2) {
        continue;
      }
      if (!input.contains(token.getTerm())) {
        input.add(token.getTerm());
      }
    }
    input.add(city.getCityname());
    Completion completion = new Completion(list2String(input));
    city.setSuggesttag(completion);
    return true;
  }

  @Override
  public List<String> suggest(String prefix) {
    CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("complete").text(prefix).field("suggesttag").size(5);
    SuggestBuilder suggestBuilder = new SuggestBuilder();
    suggestBuilder.addSuggestion(suggestion);
    SearchResponse response = this.esClient.prepareSearch("cityindex").setTypes("city").addSuggestion(suggestion).execute().actionGet();
    Suggest suggest = response.getSuggest();
    // 没有任何数据
    if (suggest == null) {
      return new ArrayList<String>();
    }
    List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> list = response.getSuggest().getSuggestion("complete").getEntries();
    List<String> suggestList = new ArrayList<String>();
    if (list == null) {
      return null;
    } else {
      for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> e : list) {
        for (Suggest.Suggestion.Entry.Option option : e) {
          suggestList.add(option.getText().toString());
        }
      }
    }
    return suggestList;
  }

  public String[] list2String(List<String> list) {
    if (list == null || list.isEmpty()) {
      return null;
    } else {
      String[] data = new String[list.size()];
      for (int i = 0; i < list.size(); i++) {
        data[i] = list.get(i);
      }
      return data;
    }
  }
}
