package com.movieflex.service;

import com.movieflex.dto.MovieDto;
import com.movieflex.dto.MoviePageResponse;
import com.movieflex.entities.Movie;
import com.movieflex.exceptions.FileExistsException;
import com.movieflex.exceptions.MovieNotFoundException;
import com.movieflex.repositories.MovieRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService{


    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }
    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. upload the file

        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File already exists! Please enter another file name!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        // 2. set the value of field 'poster' as filename
        movieDto.setPoster(uploadedFileName);

        // 3. map dto to Movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 4. save the movie object -> saved Movie object
        Movie savedMovie = movieRepository.save(movie);

        // 5. generate the posterUrl
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        // 6. map Movie object to DTO object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        //1.check the data if present return the data
    Movie movie=movieRepository.findById(movieId).orElseThrow(()->new MovieNotFoundException("movie not found with movie id "+ movieId));

        //2.generate poster url
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        //3.map to movie repository and return
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {

        //1.to fetch all data from db
        List<Movie> movieLst=movieRepository.findAll();

        List<MovieDto> movieDtos=new ArrayList<>();

        //2.iterate threw the list and generate poster url for each movie object and map to movie dto object
       for(Movie movie:movieLst){
           String posterUrl = baseUrl + "/file/" + movie.getPoster();
           MovieDto movieDto = new MovieDto(
                   null,
                   movie.getTitle(),
                   movie.getDirector(),
                   movie.getStudio(),
                   movie.getMovieCast(),
                   movie.getReleaseYear(),
                   movie.getPoster(),
                   posterUrl
           );
           movieDtos.add(movieDto);
       }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        //1.check if movie exist with given id
        Movie mv=movieRepository.findById(movieId).orElseThrow(()->new MovieNotFoundException("movie not found with movie id "+ movieId));

        //2.if file is null do nothing but if file is not null delete existing file associate with recored and update the new file.
            String fileName=mv.getPoster();
            if(file!=null){
                Files.deleteIfExists(Paths.get(path+File.separator+fileName));
                fileName=fileService.uploadFile(path,file);

            }
        //3.set movie dto poster value according to step 2
            movieDto.setPoster(fileName);
        //4.map it to movie object and save the movie object
        Movie movienew = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        //5. generate poster url
        Movie updatedMovie=movieRepository.save(movienew);

        String posterUrl = baseUrl + "/file/" + fileName;

        //3.map to movie repository and return
        MovieDto response = new MovieDto(
                movienew.getMovieId(),
                movienew.getTitle(),
                movienew.getDirector(),
                movienew.getStudio(),
                movienew.getMovieCast(),
                movienew.getReleaseYear(),
                movienew.getPoster(),
                posterUrl
        );
        return response;

    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {

        Movie mv=movieRepository.findById(movieId).orElseThrow(()->new MovieNotFoundException("movie not found with movie id "+ movieId));

        Integer id=mv.getMovieId();
        Files.deleteIfExists(Paths.get(path+File.separator+mv.getPoster()));

        movieRepository.delete(mv);

        return "movie deleted with id ="+ id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable= PageRequest.of(pageNumber,pageSize);
        Page<Movie> moviePages =  movieRepository.findAll(pageable);
               List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos=new ArrayList<>();

        for(Movie movie:movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    null,
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return new MoviePageResponse(movieDtos,pageNumber,pageSize,moviePages.getTotalElements(),moviePages.getTotalPages(),moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndShorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        Sort sort= dir.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(pageNumber,pageSize,sort);
        Page<Movie> moviePages =  movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos=new ArrayList<>();

        for(Movie movie:movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    null,
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos,pageNumber,pageSize,moviePages.getTotalElements(),moviePages.getTotalPages(),moviePages.isLast());
    }


}
