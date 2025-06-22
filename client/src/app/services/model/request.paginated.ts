export interface PaginatedRequest {
    page: number
    size: number
    sortBy: string
    sortDir: string
  }

  export interface Pageable {
    pageNumber: number
    pageSize: number
    sort: Sort
    offset: number
    unpaged: boolean
    paged: boolean
  }
  
  export interface Sort {
    empty: boolean
    unsorted: boolean
    sorted: boolean
  }
  