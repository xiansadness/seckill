package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * controller的作用，根据接收的数据，通过判断来实现页面跳转的控制
 */

@Controller//如果一个普通类被Controller修饰，便可以将该类看成是一个servlet
@RequestMapping("/seckill")//相当于是模块
//RequestMapping,该注解既可以修饰类，也可以修饰方法，相当于是为类或方法配置对外访问的虚拟路径
//url：/模块/资源/{id}/细分
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //控制层调用业务层方法，所以需要先声明业务层对象，而对象的创建则交给Spring框架
    @Autowired//该注解实现依赖注入
    private SeckillService seckillService;

    /**
     * 获取所有的秒杀商品，并将其封装到model类型的数据中
     * @param model
     * @return 跳转到列表页(携带着所有的秒杀商品)
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    //method限制该方法只能处理get请求
    public String list(Model model) {//Model可以用于前端页面和控制层之间数据的传递，model中的数据实际上存放在request域中
        //前端页面可以通过EL表达式来获取Model中的数据

        //获取列表页

        List<Seckill> list = seckillService.getSeckillList();//调用业务层方法，获取秒杀商品列表
        model.addAttribute("list", list);

        return "list";
        /**
         * 注意：1.因为该controller方法没有被注解@ResponseBody修饰，所以该方法实际上实现的是页面的跳转
         *          页面的跳转有两种方式：转发与重定向，没有关键字区分时，默认为转发
         *      2.因为在spring-web.xml文件中配置了视图解析器，所以这里在指定跳转页面时，省去了前缀和后缀
         *          实际上跳转页面为 /WEB-INF/jsp/list.jsp  即跳转到WEB-INF目录下jsp文件夹中的list.jsp页面
         */
    }


    /**
     * 获取指定id的秒杀商品的详细信息
     * @param seckillId
     * @param model
     * @return 跳转到详情页
     */
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    //PathVariable注解会将{}中的seckillId传给detail方法的seckillId形参
    //相当于url中携带了传给形参的数据？
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {

        if (seckillId == null) {//如果没有给seckillId
            return "redirect:/seckill/list";//重定向，跳转到列表页
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {//如果查询结果为空，说明用户给出的seckillId无效
            return "forward:/seckill/list";//转发，跳转到列表页
        }

        //查询结果存在，则跳转到详情页面
        model.addAttribute("seckill", seckill);
        return "detail";// /WEB-INF/jsp/detail.jsp

    }

    /**
     * 获取指定id的秒杀商品，关于秒杀地址暴露的相关信息
     * @param seckillId
     * @return
     */
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody//加上该注解表示该方法不实现跳转，而是返回json类型的数据(即会将该方法的返回类型自动转化为json类型？)
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {

        SeckillResult<Exposer> result;//先声明一个结果对象

        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            //如果在执行上面业务层方法时没有异常，说明请求被正常处理？但并不表示要暴露秒杀地址
            /**
             * 也就是说能够获取到查询结果exposer,但是是否要暴露秒杀地址，还是要根据exposer中封装的信息来决定
             */
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {//如果有异常抛出，则表示请求没有被正常处理，需要返回错误信息
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }

        return result;
    }

    /**
     * 执行秒杀
     * @param seckillId
     * @param md5
     * @param phone
     * @return
     */
    @RequestMapping(value="/{seckillId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = "application/json;charset=UTF-8")
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value="killPhone", required = false) Long phone){
                //required属性默认为true，表示在调用该方法时必须传递phone参数，设置为false后则允许该参数为空
                //相当于注册或登录过的用户会有记录，未注册或未登录的用户则没有记录？

        if(phone == null){
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
        //不为空，即在cookie中留有号码,cookie中是只有一个号码还是包含所有号码？
        //cookie是浏览器端的内容，也就是是用户端的内容，应该是只有一个

        try {

            SeckillExecution seckillExecution=seckillService.executeSeckillProcedure(seckillId,phone,md5);//执行业务层方法
            return new SeckillResult<SeckillExecution>(true,seckillExecution);//请求处理成功，返回封装后的执行结果

        } catch (RepeatKillException e){//就算抛出了异常，本质上，用户发出的请求也是被成功处理了的，只是秒杀没有成功
            //这里将success的false改为true
            SeckillExecution seckillExecution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            //return new SeckillResult<SeckillExecution>(false,seckillExecution);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);

        } catch (SeckillCloseException e){
            SeckillExecution seckillExecution=new SeckillExecution(seckillId,SeckillStatEnum.END);
            //return new SeckillResult<SeckillExecution>(false,seckillExecution);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);

        } catch (SeckillException e) {
            SeckillExecution seckillExecution=new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
            //return new SeckillResult<SeckillExecution>(false,seckillExecution);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);
        }
    }

    /**
     * 获取系统当前时间
     * @return
     */
    @RequestMapping(value="/time/now",method=RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){

        Date now =new Date();
        return new SeckillResult<Long>(true,now.getTime());

    }

}
