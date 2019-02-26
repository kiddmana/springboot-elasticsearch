package org.spring.springboot.domain;

import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.completion.Completion;
import java.io.Serializable;

/**
 * 城市实体类
 *
 * Created by bysocket on 03/05/2017.
 */
@Document(indexName = "cityindex", type = "city")
public class City implements Serializable{

    private static final long serialVersionUID = -1L;

    /**
     * 城市编号
     */
    @Field(type=FieldType.Long)
    private Long id;

    /**
     * 省份编号
     */
    @Field(type=FieldType.Long)
    private Long provinceid;

    /**
     * 城市名称
     */
    @Field(type=FieldType.String,analyzer="ik",searchAnalyzer="ik")
    private String cityname;

    /**
     * 描述
     */
    @Field(type=FieldType.String)
    private String description;
    
    @CompletionField(analyzer="ik",searchAnalyzer="ik",payloads=false)
    private Completion suggesttag;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProvinceid() {
        return provinceid;
    }

    public void setProvinceid(Long provinceid) {
        this.provinceid = provinceid;
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Completion getSuggesttag() {
      return suggesttag;
    }

    public void setSuggesttag(Completion suggesttag) {
      this.suggesttag = suggesttag;
    }
    
    
}
