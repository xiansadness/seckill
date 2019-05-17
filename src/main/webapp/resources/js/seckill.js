//存放主要交互逻辑的js代码
//javascript模块化，可以类比于java中的package.类.方法


var seckill = {
    //封装秒杀相关ajax的url，将所有需要用到的url封装起来，方便后期维护
    URL: {
        now: function () {
            return '/seckill/time/now';//web层中的time()方法
        },

        exposer:function (seckillId) {
            return '/seckill/'+seckillId+'/exposer';
        },

        execution:function (seckillId,md5) {
            return '/seckill/'+seckillId+'/'+md5+'/execution';
        }

    },

    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;//直接判断对象，看对象是否为空，空就是undefine就是false; isNaN 非数字返回true
        } else {
            return false;
        }
    },

    //计时
    countDown: function (seckillId, nowTime, startTime, endTime) {

        var seckillBox = $('#seckill-box');//取出详情页面上用来显示计时器的标签

        if (nowTime > endTime) {//秒杀结束

            seckillBox.html("秒杀已结束！");

        } else if (nowTime < startTime) {//秒杀未开始,需要计时

            var killTime = new Date(startTime + 1000);//+1000ms即加1秒，是为了防止时间偏移

            seckillBox.countdown(killTime, function (event) {
                //控制时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');//格式化日期格式
                seckillBox.html(format);
                //时间完成后的回调事件
            }).on('finish.countdown', function () {//计时结束之后，则暴露秒杀地址,即可以执行秒杀
                //时间完成后回调事件
                //获取秒杀地址，控制实现逻辑，执行秒杀
                seckill.handlerSeckill(seckillId,seckillBox);
            });

        } else {//秒杀开始
            seckill.handlerSeckill(seckillId,seckillBox);
        }
    },

    //处理秒杀逻辑
    handlerSeckill:function(seckillId,node) {

        //获取秒杀地址，控制显示器，执行秒杀
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');//未判断之前，该控件被隐藏，不会显示在页面上

        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中执行交互流程
            if(result && result['success']){//请求被正确处理
                var exposer=result['data'];//data中封装的是Exposer

                if(exposer['exposed']){//为true则暴露秒杀地址,开启秒杀

                    var md5=exposer['md5'];
                    var killUrl=seckill.URL.execution(seckillId,md5);
                    console.log("killUrl:"+killUrl);

                    //绑定一次点击事件,防止服务器端处理同一url请求
                    $('#killBtn').one('click',function () {
                        //执行秒杀请求
                        //1.先禁用按钮
                        $(this).addClass('disabled');//this表示killBtn
                        //2.发送秒杀请求执行秒杀
                        $.post(killUrl,{},function (result) {//发送完请求后，分析请求结果
                            if(result && result['success']){
                                var killResult=result['data'];
                                var state=killResult['state'];
                                var stateInfo=killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });
                    node.show();
                }else{//未开启秒杀
                    var now=exposer['now'];
                    var start=exposer['start'];
                    var end=exposer['end'];
                    //重新进行计时逻辑
                    seckill.countDown(seckillId,now,start,end);
                }
            }else{
                console.log('result:'+result);
            }
        });


    },


    //详情页秒杀逻辑
    detail: {
        //详情页面初始化
        init: function (params) {

            /*登录逻辑实现*/

            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');

            //验证手机号
            if (!seckill.validatePhone(killPhone)) {//之前没有登录
                //绑定手机 控制输出
                var killPhoneModal = $('#killPhoneModal');//取出detail.jsp中的弹出层，根据标签的id来取
                killPhoneModal.modal({
                    show: true,//显示弹出层，默认是隐藏的
                    backdrop: 'static',//禁止位置关闭，即在没有填写手机号之前不能关闭该弹出层
                    keyboard: false//关闭键盘事件
                });
                //对弹出层的按钮做点击事件绑定
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();//先取到用户输入的信息
                    console.log("inputPhone" + inputPhone);

                    if (seckill.validatePhone(inputPhone)) {//对用户的输入信息进行验证，验证是否符合格式要求，即输入的必须是11位的电话号码
                        //通过验证，将输入信息写入cookie，并刷新页面

                        //电话写入cookie(7天过期)
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});//cookie只在秒杀路径下有效
                        //验证通过 刷新页面
                        window.location.reload();

                    } else {//未通过验证
                        //实际开发时，最好将错误文案放入前端字典中去
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误！</label>').show(300);
                    }

                });
            }

            /*已登录后，计时逻辑的实现*/

            var startTime = params['startTime'];//拿到传递过来的参数
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];

            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间判断，计时交互
                    seckill.countDown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result:' + result);
                }

            });
        }

    }
}