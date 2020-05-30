# Spring StopWatch
- Author: [HuiFer](https://github.com/huifer)
- Դ���Ķ��ֿ�: [SourceHot-spring](https://github.com/SourceHot/spring-framework-read)

- ȫ·��: `org.springframework.util.StopWatch`
## ����
- taskList: ������Ϣ�б�
- keepTaskList: �Ƿ���������Ϣ�б�
- startTimeMillis: ����ʼ��ʱ��
- currentTaskName: ��������
- lastTaskInfo: ������Ϣ
- taskCount: ��������
- totalTimeMillis: �ܹ����ѵ�ʱ��

## ����
- `org.springframework.util.StopWatch.start(java.lang.String)`
```java
    public void start(String taskName) throws IllegalStateException {
        if (this.currentTaskName != null) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.currentTaskName = taskName;
        this.startTimeMillis = System.currentTimeMillis();
    }
```
- `org.springframework.util.StopWatch.stop`
```java
    public void stop() throws IllegalStateException {
        if (this.currentTaskName == null) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }
        // ���ѵ�ʱ��
        long lastTime = System.currentTimeMillis() - this.startTimeMillis;
        this.totalTimeMillis += lastTime;
        // ������Ϣ��ʼ��
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (this.keepTaskList) {
            this.taskList.add(this.lastTaskInfo);
        }
        ++this.taskCount;
        this.currentTaskName = null;
    }

```