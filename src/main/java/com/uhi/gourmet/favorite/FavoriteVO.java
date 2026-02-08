package com.uhi.gourmet.favorite;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class FavoriteVO {

    private int favorite_id;
    private String user_id;
    private int store_id;
    private Date created_at;
    private String store_name;
    private String store_img;
    private String store_category;
    private double avg_rating;


}
