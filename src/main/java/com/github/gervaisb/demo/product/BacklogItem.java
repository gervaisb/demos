package com.github.gervaisb.demo.product;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

public class BacklogItem {

    final BacklogItemState state;

    public BacklogItem(String name) {
        this.state = new BacklogItemState();
        this.state.name = name;
    }

    BacklogItem(BacklogItemState state) {
        this.state = state;
    }
}

@Data
@Entity(name="backlog_item")
class BacklogItemState {

    @Id
    @GeneratedValue
    Long id;

    String key;
    String name;

    BacklogItem toProductBacklogItem() {
        return new BacklogItem(this);
    }
}
