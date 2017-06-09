package com.sqrtf.common.model;

import java.util.List;

/**
 * Created by roya on 2017/5/27.
 */

public class EpisodeDetail {
    public Integer status;
    public Integer episode_no;
    public Long update_time;
    public String name;
    public Integer bgm_eps_id;
    public String bangumi_id;
    public String airdate;
    public String name_cn;
    public String thumbnail;
    public Long delete_mark;
    public Long create_time;
    public String duration;
    public String id;

    public List<VideoFile> video_files;
}
