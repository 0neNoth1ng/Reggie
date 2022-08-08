package com.hcb.dto;

import com.hcb.entity.Setmeal;
import com.hcb.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
