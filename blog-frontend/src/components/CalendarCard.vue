<template>
  <div class="card">
    <div class="card-title">日历</div>
    <div class="calendar-content">
      <div class="calendar-header">
        <el-icon class="nav-btn" @click="prevMonth"><ArrowLeft /></el-icon>
        <span class="current-month">{{ currentYear }}年{{ currentMonth }}月</span>
        <el-icon class="nav-btn" @click="nextMonth"><ArrowRight /></el-icon>
      </div>
      
      <div class="calendar-weekdays">
        <span v-for="day in weekdays" :key="day" class="weekday">{{ day }}</span>
      </div>
      
      <div class="calendar-days">
        <span 
          v-for="(day, index) in calendarDays" 
          :key="index"
          class="day"
          :class="{
            'empty': !day.date,
            'today': day.isToday,
            'has-article': day.hasArticle
          }"
        >
          <span v-if="day.date" class="day-number">{{ day.date }}</span>
          <span v-if="day.hasArticle" class="article-line"></span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { articleApi } from '@/api/article'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const articleDates = ref([])

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1).getDay()
  const daysInMonth = new Date(currentYear.value, currentMonth.value, 0).getDate()
  const today = new Date()
  
  for (let i = 0; i < firstDay; i++) {
    days.push({ date: null, hasArticle: false, isToday: false })
  }
  
  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const isToday = today.getFullYear() === currentYear.value && 
                    today.getMonth() + 1 === currentMonth.value && 
                    today.getDate() === i
    const hasArticle = articleDates.value.includes(dateStr)
    
    days.push({ 
      date: i, 
      hasArticle,
      isToday
    })
  }
  
  return days
})

const loadArticleDates = async () => {
  const res = await articleApi.getDates(currentYear.value, currentMonth.value)
  if (res.code === 200) {
    articleDates.value = res.data.map(d => {
      if (typeof d === 'string') return d.split('T')[0]
      return d
    })
  }
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

watch([currentYear, currentMonth], () => {
  loadArticleDates()
})

onMounted(() => {
  loadArticleDates()
})
</script>

<style scoped lang="scss">
.calendar-content {
  .calendar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 15px;
    
    .current-month {
      font-weight: 600;
      font-size: 14px;
    }
    
    .nav-btn {
      cursor: pointer;
      padding: 5px;
      border-radius: 4px;
      transition: background 0.2s;
      
      &:hover {
        background: #f0f0f0;
      }
    }
  }
  
  .calendar-weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    margin-bottom: 10px;
    
    .weekday {
      text-align: center;
      font-size: 12px;
      color: #999;
      padding: 5px 0;
    }
  }
  
  .calendar-days {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 2px;
    
    .day {
      position: relative;
      text-align: center;
      padding: 8px 0;
      font-size: 13px;
      border-radius: 4px;
      
      &.empty {
        visibility: hidden;
      }
      
      &.today {
        .day-number {
          background: #409eff;
          color: #fff;
          border-radius: 50%;
          width: 24px;
          height: 24px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }
      }
      
      &.has-article {
        .article-line {
          position: absolute;
          bottom: 2px;
          left: 50%;
          transform: translateX(-50%);
          width: 16px;
          height: 3px;
          background: #67c23a;
          border-radius: 2px;
        }
      }
      
      .day-number {
        display: inline-block;
      }
    }
  }
}
</style>
