package com.uhi.gourmet.photo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class PhotoVO {

    private int photo_id;
    private int store_id;
    private String file_path;
    private String original_name;
    private String is_thumbnail;
    private String is_active;
    private int sort_order;
    private Date created_at;
    private Date updated_at;

}
