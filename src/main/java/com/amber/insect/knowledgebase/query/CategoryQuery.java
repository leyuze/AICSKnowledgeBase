package com.amber.insect.knowledgebase.query;

import com.amber.insect.knowledgebase.common.RPage;
import lombok.Data;

@Data
public class CategoryQuery extends RPage {
    @Override
    public String toString() {
        return "CategoryQuery{} " + super.toString();
    }
}
